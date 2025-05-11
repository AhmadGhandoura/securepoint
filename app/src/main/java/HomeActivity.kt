package com.example.securepoint

import android.content.pm.PackageManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
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
import com.google.firebase.messaging.FirebaseMessaging

class HomeActivity : AppCompatActivity() {

    private var role: String = "guest"
    private lateinit var databaseRef: DatabaseReference
    private lateinit var notificationManager: NotificationManager

    private var lastGasAlert = false
    private var lastVibration = false
    private var lastWindow = 0
    private var lastMotion = 0
    private var lastNotifyTime = 0L

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

        findViewById<TextView>(R.id.roleBadge).apply {
            text = if (role == "admin") "🛡️ Admin" else "🙋 Guest"
            setBackgroundColor(ContextCompat.getColor(this@HomeActivity, R.color.light_lime_green))
        }

        findViewById<ImageButton>(R.id.menu_button).setOnClickListener {
            showPopupMenu(it)
        }

        if (role == "admin") {
            checkNotificationPermission()
            setupRealtimeAlerts()
        }

        // ✅ Subscribe to 'alerts' topic
        FirebaseMessaging.getInstance().subscribeToTopic("alerts")
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("🔔FCM", "Subscribed to 'alerts' topic")
                } else {
                    Log.e("🔔FCM", "Subscription failed", task.exception)
                }
            }

        // Optional: Log token (can be removed later)
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                Log.d("📱FCM_TOKEN", "Token: $token")
            }
        }
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
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
                "sensor_alerts", "Sensor Alerts", NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Sensor alert channel"
                enableLights(true)
                lightColor = Color.RED
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val now = System.currentTimeMillis()
                if (now - lastNotifyTime < 4000) return

                val gasAlert = snapshot.child("gasAlert").getValue(Boolean::class.java) ?: false
                val vibration = snapshot.child("vibration").getValue(Boolean::class.java) ?: false
                val window = snapshot.child("window").getValue(Int::class.java) ?: 0
                val motion = snapshot.child("motion").getValue(Int::class.java) ?: 0

                if (gasAlert && !lastGasAlert) {
                    showSystemNotification("⚠️ Gas Alert", "Dangerous gas detected")
                    lastNotifyTime = now
                }

                if (vibration && !lastVibration) {
                    showSystemNotification("💥 Vibration", "Possible tampering")
                    lastNotifyTime = now
                }

                if (window == 1 && lastWindow == 0) {
                    showSystemNotification("🚪 Window Open", "A window was opened")
                    lastNotifyTime = now
                }

                if (motion == 1 && lastMotion == 0) {
                    showSystemNotification("👀 Motion Detected", "Someone's moving!")
                    lastNotifyTime = now
                }

                lastGasAlert = gasAlert
                lastVibration = vibration
                lastWindow = window
                lastMotion = motion
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "❌ ${error.message}")
            }
        })
    }

    private fun showSystemNotification(title: String, message: String) {
        val intent = Intent(this, HomeActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(this, "sensor_alerts")
            .setSmallIcon(R.drawable.ic_notify)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setColor(Color.RED)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)

        notificationManager.notify((System.currentTimeMillis() % 10000).toInt(), builder.build())
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
                    startActivity(Intent(this, AdminDashboardActivity::class.java)); true
                }
                R.id.menu_profile -> {
                    val intent = Intent(this, ProfileActivity::class.java)
                    intent.putExtra("userRole", role)
                    startActivity(intent); true
                }
                R.id.menu_logout -> {
                    FirebaseAuth.getInstance().signOut()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish(); true
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