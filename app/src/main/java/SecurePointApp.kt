package com.example.securepoint

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.database.*

class SecurePointApp : Application() {

    private val channelId = "sensor_alerts"
    private val channelName = "Sensor Alerts"
    private lateinit var databaseRef: DatabaseReference
    private lateinit var notificationManager: NotificationManager

    override fun onCreate() {
        super.onCreate()

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
        startSensorListener()
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

    private fun startSensorListener() {
        databaseRef = FirebaseDatabase.getInstance().reference.child("sensors")

        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val gasAlert = snapshot.child("gasAlert").getValue(Boolean::class.java) ?: false
                val vibration = snapshot.child("vibration").getValue(Boolean::class.java) ?: false
                val window = snapshot.child("window").getValue(Int::class.java) ?: 0

                if (gasAlert) sendSystemNotification("⚠️ Gas Alert", "Dangerous gas detected")
                if (vibration) sendSystemNotification("💥 Vibration Alert", "Vibration detected at home")
                if (window == 1) sendSystemNotification("🚪 Window Open", "Window was opened")
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun sendSystemNotification(title: String, message: String) {
        val intent = Intent(this, HomeActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notify)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify((System.currentTimeMillis() % 10000).toInt(), notification)
    }
}
