package com.example.securepoint

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlin.math.abs

class HomeActivity : AppCompatActivity() {

    private var role: String = "guest"
    private lateinit var databaseRef: DatabaseReference
    private lateinit var notificationManager: NotificationManager

    private var lastGasAlert = false
    private var lastVibration = false
    private var lastWindow = 0

    private var lastGasAlertTime = 0L
    private var lastVibrationTime = 0L
    private var lastWindowTime = 0L

    private val COOLDOWN_MILLIS = 60 * 1000L // 1 minute

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_activity)

        role = intent.getStringExtra("userRole") ?: "guest"

        if (role == "guest") {
            findViewById<CardView>(R.id.cameraButton).visibility = View.GONE
            findViewById<CardView>(R.id.sensorsButton).visibility = View.GONE
            findViewById<CardView>(R.id.windowsButton).visibility = View.GONE
            findViewById<MaterialButton>(R.id.btn_generate_pdf_home).visibility = View.GONE
        }

        val badge = findViewById<TextView>(R.id.roleBadge)
        badge.text = if (role == "admin") "🛡️ Admin" else "🙋 Guest"
        badge.setBackgroundColor(ContextCompat.getColor(this, R.color.light_lime_green))

        findViewById<ImageButton>(R.id.menu_button).setOnClickListener {
            showPopupMenu(it)
        }

        if (role == "admin") {
            checkNotificationPermission()
            setupRealtimeAlerts()
        }
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    101
                )
            }
        }
    }

    private fun setupRealtimeAlerts() {
        databaseRef = FirebaseDatabase.getInstance().getReference("sensors")
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "sensor_alerts",
                "Sensor Alerts",
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.description = "Sensor alert channel"
            channel.enableLights(true)
            channel.lightColor = Color.RED
            channel.enableVibration(true)
            notificationManager.createNotificationChannel(channel)
        }

        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val gasAlert = snapshot.child("gasAlert").getValue(Boolean::class.java) ?: false
                val vibration = snapshot.child("vibration").getValue(Boolean::class.java) ?: false
                val window = snapshot.child("window").getValue(Int::class.java) ?: 0

                val now = System.currentTimeMillis()

                if (gasAlert && (!lastGasAlert || now - lastGasAlertTime > COOLDOWN_MILLIS)) {
                    showSystemNotification("⚠️ Gas Alert", "Dangerous gas detected")
                    lastGasAlertTime = now
                }

                if (vibration && (!lastVibration || now - lastVibrationTime > COOLDOWN_MILLIS)) {
                    showSystemNotification("💥 Vibration Detected", "Possible tampering")
                    lastVibrationTime = now
                }

                if (window == 1 && (lastWindow == 0 || now - lastWindowTime > COOLDOWN_MILLIS)) {
                    showSystemNotification("🚪 Window Open", "A window was opened")
                    lastWindowTime = now
                }

                lastGasAlert = gasAlert
                lastVibration = vibration
                lastWindow = window
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "❌ Error: ${error.message}")
            }
        })
    }

    private fun showSystemNotification(title: String, message: String) {
        val intent = Intent(this, HomeActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, "sensor_alerts")
            .setSmallIcon(R.drawable.ic_notify)
            .setContentTitle(title)
            .setContentText(message)
            .setColor(Color.RED)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val id = (System.currentTimeMillis() % 10000).toInt()
        notificationManager.notify(id, notification)
    }

    private fun showPopupMenu(anchor: View) {
        val popup = PopupMenu(this, anchor)
        popup.menuInflater.inflate(R.menu.home_menu, popup.menu)

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

    fun openDoors(view: View) {
        val intent = Intent(this, DoorActivity::class.java)
        intent.putExtra("userRole", role)
        startActivity(intent)
    }

    fun openNFC(view: View) {
        startActivity(Intent(this, NFCUnlockActivity::class.java))
    }

    fun openNotifications(view: View) {
        startActivity(Intent(this, NotificationActivity::class.java))
    }

    fun pdfReport(view: View) {
        startActivity(Intent(this, PDFReportActivity::class.java))
    }
}