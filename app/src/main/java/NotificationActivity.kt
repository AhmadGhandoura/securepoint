package com.example.securepoint

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.google.firebase.database.*

class NotificationActivity : AppCompatActivity() {

    private lateinit var databaseRef: DatabaseReference
    private lateinit var notificationManager: NotificationManager

    private lateinit var listView: ListView
    private lateinit var adapter: ArrayAdapter<String>
    private val alertMessages = mutableListOf<String>()

    private val channelId = "sensor_alerts"
    private val channelName = "Sensor Alerts"
    private var lastGasAlert = false
    private var lastVibration = false
    private var lastWindow = 0
    private var lastMotion = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.notification_activity)

        listView = findViewById(R.id.lv_notifications)
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, alertMessages)
        listView.adapter = adapter

        databaseRef = FirebaseDatabase.getInstance().reference.child("sensors")
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        createNotificationChannel()
        listenToSensorAlerts()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Sensor alert channel"
                enableLights(true)
                lightColor = Color.RED
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun listenToSensorAlerts() {
        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val gasAlert = snapshot.child("gasAlert").getValue(Boolean::class.java) ?: false
                val vibration = snapshot.child("vibration").getValue(Boolean::class.java) ?: false
                val window = snapshot.child("window").getValue(Int::class.java) ?: 0
                val motion = snapshot.child("motion").getValue(Int::class.java) ?: 0

                if (gasAlert && !lastGasAlert) {
                    showAndLogAlert("⚠️ Gas Alert", "Dangerous gas detected")
                }
                if (vibration && !lastVibration) {
                    showAndLogAlert("💥 Vibration Detected", "Possible tampering or shaking")
                }
                if (window == 1 && lastWindow == 0) {
                    showAndLogAlert("🚪 Window Open", "Window has been opened")
                }
                if (motion == 1 && lastMotion == 0) {
                    showAndLogAlert("👀 Motion Detected", "Movement has been detected")
                }

                // Store last state to prevent spamming
                lastGasAlert = gasAlert
                lastVibration = vibration
                lastWindow = window
                lastMotion = motion
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun showAndLogAlert(title: String, message: String) {
        val intent = Intent(this, HomeActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notify)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setColor(Color.RED)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .build()

        val notificationId = (System.currentTimeMillis() % 10000).toInt()
        notificationManager.notify(notificationId, notification)

        val log = "$title - ${System.currentTimeMillis()}"
        alertMessages.add(0, log)
        adapter.notifyDataSetChanged()
    }
}