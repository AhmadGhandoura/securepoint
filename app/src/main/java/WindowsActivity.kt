package com.example.securepoint

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class WindowsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.windows_activity)

        val windowStatus = findViewById<TextView>(R.id.tv_window_status)
        val refreshButton = findViewById<Button>(R.id.btn_refresh_windows)

        refreshButton.setOnClickListener {
            // Simulate refreshing window status
            windowStatus.text = "Windows are currently OPEN" // Example data
        }
    }
}
