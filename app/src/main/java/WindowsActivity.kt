package com.example.securepoint

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*

class WindowsActivity : AppCompatActivity() {

    private lateinit var windowStatus: TextView
    private lateinit var refreshButton: Button
    private lateinit var databaseRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.windows_activity)

        windowStatus = findViewById(R.id.tv_window_status)
        refreshButton = findViewById(R.id.btn_refresh_windows)

        // ✅ Corrected path
        databaseRef = FirebaseDatabase.getInstance().getReference("sensors/window")

        // Initial fetch
        fetchWindowStatus()

        refreshButton.setOnClickListener {
            fetchWindowStatus()
        }
    }

    private fun fetchWindowStatus() {
        databaseRef.get().addOnSuccessListener { snapshot ->
            val windowValue = (snapshot.getValue(Long::class.java) ?: 0L).toInt()
            windowStatus.text = if (windowValue == 0) {
                "🪟 Windows are CLOSED"
            } else {
                "🚨 Windows are OPEN"
            }
        }.addOnFailureListener {
            Toast.makeText(this, "❌ Failed to load window status", Toast.LENGTH_SHORT).show()
        }
    }
}