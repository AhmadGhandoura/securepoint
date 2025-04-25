package com.example.securepoint

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth

class HomeActivity : AppCompatActivity() {

    private var role: String = "guest"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_activity)

        // 🔓 Get the role from intent
        role = intent.getStringExtra("userRole") ?: "guest"

        // 🔐 Hide features for guest users
        if (role == "guest") {
            findViewById<LinearLayout>(R.id.cameraButton).visibility = View.GONE

            findViewById<LinearLayout>(R.id.sensorsButton).visibility = View.GONE
            findViewById<LinearLayout>(R.id.windowsButton).visibility = View.GONE
            findViewById<LinearLayout>(R.id.doorsButton).visibility = View.GONE
        }

        // 🔖 Set badge text and color
        val badge = findViewById<TextView>(R.id.roleBadge)
        badge.text = if (role == "admin") "🛡️ Admin" else "🙋 Guest"
        badge.setBackgroundColor(ContextCompat.getColor(this, R.color.light_lime_green))

        // 🧭 Admin Panel Button (old approach)
        if (role == "admin") {
            val btn = Button(this).apply {
                text = "Open Admin Panel"
                setBackgroundColor(ContextCompat.getColor(this@HomeActivity, R.color.light_lime_green))
                setTextColor(ContextCompat.getColor(this@HomeActivity, android.R.color.black))
                textSize = 16f
                setOnClickListener {
                    startActivity(Intent(this@HomeActivity, AdminDashboardActivity::class.java))
                }
            }
            findViewById<LinearLayout>(R.id.adminPanelContainer).addView(btn)
        }

        // 🍔 Setup menu button
        val menuButton = findViewById<ImageButton>(R.id.menu_button)
        menuButton.setOnClickListener {
            showPopupMenu(menuButton)
        }
    }

    private fun showPopupMenu(anchor: View) {
        val popup = PopupMenu(this, anchor)
        popup.menuInflater.inflate(R.menu.home_menu, popup.menu)

        // Hide Admin option if guest
        if (role == "guest") {
            popup.menu.findItem(R.id.menu_admin_dashboard).isVisible = false
        }

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_admin_dashboard -> {
                    startActivity(Intent(this, AdminDashboardActivity::class.java))
                    true
                }
                R.id.menu_profile -> {
                    val intent = Intent(this, ProfileActivity::class.java)
                    intent.putExtra("userRole", role)
                    startActivity(intent)
                    true
                }
                R.id.menu_logout -> {
                    FirebaseAuth.getInstance().signOut()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    // ✅ Admin-only methods
    fun openCamera(view: View) {
        if (role != "admin") return
        startActivity(Intent(this, CameraActivity::class.java))
    }

    fun openSensors(view: View) {
        if (role != "admin") return
        startActivity(Intent(this, SensorActivity::class.java))
    }

    fun openWindows(view: View) {
        if (role != "admin") return
        startActivity(Intent(this, WindowsActivity::class.java))
    }

    // ✅ Accessible by everyone
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
