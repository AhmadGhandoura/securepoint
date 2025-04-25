package com.example.securepoint

import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.firebase.database.*

class SensorActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference

    private lateinit var container: ConstraintLayout
    private lateinit var tvMotionSensor: TextView
    private lateinit var tvVibrationSensor: TextView
    private lateinit var tvGasAlert: TextView
    private lateinit var tvWindowSensor: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sensor_activity)

        // Firebase reference
        database = FirebaseDatabase.getInstance().getReference("door/sensors")

        // UI elements
        container = findViewById(R.id.sensor_container)
        tvMotionSensor = findViewById(R.id.tv_motion_sensor)
        tvVibrationSensor = findViewById(R.id.tv_vibration_sensor)
        tvGasAlert = findViewById(R.id.tv_gas_sensor)
        tvWindowSensor = findViewById(R.id.tv_window_sensor)

        // 🔄 Listen live to Firebase
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val motion = (snapshot.child("motion").getValue(Long::class.java) ?: 0L).toInt() == 1
                val vibration = snapshot.child("vibration").getValue(Boolean::class.java) ?: false
                val window = (snapshot.child("window").getValue(Long::class.java) ?: 0L).toInt()
                val gasVal = snapshot.child("gasAnalog").getValue(Long::class.java) ?: 0L
                val gasAlert = snapshot.child("gasAlert").getValue(Boolean::class.java) ?: false

                // 🧠 Update UI
                tvMotionSensor.text = if (motion) "Motion: 🚶 Detected" else "Motion: 🌙 No motion"
                tvVibrationSensor.text = if (vibration) "Vibration: ⚠️ Detected" else "Vibration: ✅ Normal"
                tvWindowSensor.text = if (window == 0) "Window: 🪟 Closed" else "Window: 🚨 Open"
                tvGasAlert.text = "Gas: $gasVal ppm | ${if (gasAlert) "🔥 ALERT" else "✅ Safe"}"

                // 🔥 Change background color if alert
                if (gasAlert || vibration) {
                    container.setBackgroundColor(Color.parseColor("#FF4444")) // Red
                } else {
                    container.setBackgroundColor(resources.getColor(R.color.app_grey, theme)) // Normal
                }
            }

            override fun onCancelled(error: DatabaseError) {
                tvMotionSensor.text = "❌ Error loading data: ${error.message}"
            }
        })
    }
}
