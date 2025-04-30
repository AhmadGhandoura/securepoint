package com.example.securepoint

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*

class NfcTagManagerActivity : AppCompatActivity() {

    private lateinit var databaseRef: DatabaseReference
    private lateinit var listView: ListView
    private lateinit var adapter: ArrayAdapter<String>
    private val uidList = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nfc_tag_manager)

        databaseRef = FirebaseDatabase.getInstance().reference.child("authorizedUIDs")

        listView = findViewById(R.id.lv_nfc_tags)
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, uidList)
        listView.adapter = adapter

        loadUids()

        val etUid = findViewById<EditText>(R.id.et_uid)
        val btnAdd = findViewById<Button>(R.id.btn_add_uid)

        btnAdd.setOnClickListener {
            val uid = etUid.text.toString().uppercase().trim()
            if (uid.length >= 4) {
                databaseRef.child(uid).setValue(true)
                etUid.setText("")
                Toast.makeText(this, "✅ UID added", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "⚠️ Invalid UID", Toast.LENGTH_SHORT).show()
            }
        }

        listView.setOnItemLongClickListener { _, _, position, _ ->
            val uidToDelete = uidList[position]
            databaseRef.child(uidToDelete).removeValue()
            Toast.makeText(this, "🗑️ UID removed", Toast.LENGTH_SHORT).show()
            true
        }
    }

    private fun loadUids() {
        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                uidList.clear()
                for (uidSnap in snapshot.children) {
                    uidList.add(uidSnap.key ?: "")
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}
