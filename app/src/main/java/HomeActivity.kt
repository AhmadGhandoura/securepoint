package com.example.securepoint

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_activity)
    }

    // ✅ Ensure all these methods exist in this file
    fun openCamera(view: View) {
        startActivity(Intent(this, CameraActivity::class.java))
    }

    fun openSensors(view: View) {
        startActivity(Intent(this, SensorActivity::class.java))
    }

    fun openWindows(view: View) {
        startActivity(Intent(this, WindowsActivity::class.java))
    }

    fun openDoors(view: View) {
        startActivity(Intent(this, DoorActivity::class.java))
    }

    fun openNFC(view: View) {
        startActivity(Intent(this, NFCUnlockActivity::class.java))
    }

    fun openNotifications(view: View) {
        startActivity(Intent(this, NotificationActivity::class.java))
    }
}
