package com.example.securepoint

import android.app.PendingIntent
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase

class NFCUnlockActivity : AppCompatActivity() {

    private var nfcAdapter: NfcAdapter? = null
    private lateinit var statusText: TextView

    private var lastSentUid: String? = null // ⛔ Prevent resending same tag repeatedly

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.nfc_activity)

        statusText = findViewById(R.id.tv_nfc_status)
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        when {
            nfcAdapter == null -> {
                statusText.text = "❌ NFC not supported on this device"
            }
            !nfcAdapter!!.isEnabled -> {
                statusText.text = "⚠️ NFC is disabled"
            }
            else -> {
                statusText.text = "✅ Tap your NFC tag to unlock"
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val intent = Intent(this, javaClass).addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_MUTABLE)
        nfcAdapter?.enableForegroundDispatch(this, pendingIntent, null, null)
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter?.disableForegroundDispatch(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        val tag: Tag? = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
        tag?.let {
            val uid = tag.id.joinToString("") { byte -> "%02X".format(byte) }.uppercase()
            statusText.text = "✅ Tag detected: $uid"

            // ✅ Prevent sending duplicate unlock requests
            if (uid == lastSentUid) {
                Toast.makeText(this, "ℹ️ UID already sent", Toast.LENGTH_SHORT).show()
                return
            }

            lastSentUid = uid // Remember last UID

            // 🔥 Push to Firebase
            val ref = FirebaseDatabase.getInstance().getReference("door/unlockRequest/fromApp")
            ref.setValue(uid).addOnSuccessListener {
                Toast.makeText(this, "✅ UID sent to door system 🔓", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                Toast.makeText(this, "❌ Failed to send UID", Toast.LENGTH_SHORT).show()
                lastSentUid = null // Allow retry on failure
            }
        }
    }
}