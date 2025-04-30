package com.example.securepoint

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class DoorActivity : AppCompatActivity() {

    private lateinit var doorStatusText: TextView
    private lateinit var lastAccessText: TextView
    private lateinit var unlockButton: Button
    private lateinit var progressBar: ProgressBar

    private lateinit var dbRef: DatabaseReference
    private val auth = FirebaseAuth.getInstance()

    private var isAdmin = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.door_activity)

        doorStatusText = findViewById(R.id.tv_door_status)
        lastAccessText = findViewById(R.id.tv_last_access)
        unlockButton = findViewById(R.id.btn_unlock_door)
        progressBar = findViewById(R.id.door_progress)

        dbRef = FirebaseDatabase.getInstance().getReference("door")

        // Check if user is admin
        val role = intent.getStringExtra("userRole") ?: "guest"
        isAdmin = role == "admin"
        unlockButton.visibility = if (isAdmin) View.VISIBLE else View.GONE

        unlockButton.setOnClickListener {
            triggerRemoteUnlock()
        }

        // Live updates from Firebase
        listenToDoorStatus()
    }

    private fun listenToDoorStatus() {
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val locked = snapshot.child("locked").getValue(Boolean::class.java) ?: true
                val access = snapshot.child("lastAccess").getValue(String::class.java) ?: "Unknown"

                doorStatusText.text = if (!locked) "🔓 Unlocked" else "🔒 Locked"
                lastAccessText.text = "Last access: $access"
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@DoorActivity, "Failed to load door data", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun triggerRemoteUnlock() {
        progressBar.visibility = View.VISIBLE
        dbRef.child("locked").setValue(false)
        dbRef.child("lastAccess").setValue("Remote by: ${auth.currentUser?.email ?: "admin"}")
            .addOnCompleteListener {
                progressBar.visibility = View.GONE
                if (it.isSuccessful) {
                    Toast.makeText(this, "🚪 Unlock command sent", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "❌ Failed to unlock", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
