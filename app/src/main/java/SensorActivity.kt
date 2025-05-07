package com.example.securepoint

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*

class SensorActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference

    private lateinit var tvMotionSensor: TextView
    private lateinit var tvVibrationSensor: TextView
    private lateinit var tvGasAlert: TextView
    private lateinit var tvWindowSensor: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sensor_activity)

        // 🔗 Reference to /sensors node
        database = FirebaseDatabase.getInstance().getReference("sensors")

        // 🧱 Initialize views
        tvMotionSensor = findViewById(R.id.tv_motion_sensor)
        tvVibrationSensor = findViewById(R.id.tv_vibration_sensor)
        tvGasAlert = findViewById(R.id.tv_gas_sensor)
        tvWindowSensor = findViewById(R.id.tv_window_sensor)

        listenToSensorChanges()
    }

    private fun listenToSensorChanges() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val motion = (snapshot.child("motion").getValue(Long::class.java) ?: 0L).toInt() == 1
                val vibration = snapshot.child("vibration").getValue(Boolean::class.java) ?: false
                val window = (snapshot.child("window").getValue(Long::class.java) ?: 0L).toInt()
                val gasVal = snapshot.child("gasAnalog").getValue(Long::class.java) ?: 0L
                val gasAlert = snapshot.child("gasAlert").getValue(Boolean::class.java) ?: false

                // 🖥️ Update UI
                tvMotionSensor.text = if (motion) "🚶 Motion Detected" else "🌙 No Motion"
                tvVibrationSensor.text = if (vibration) "💥 Vibration Detected" else "✅ No Vibration"
                tvWindowSensor.text = if (window == 0) "🪟 Window Closed" else "🚨 Window Open"
                tvGasAlert.text = "🔥 Gas: $gasVal ppm | ${if (gasAlert) "⚠️ ALERT" else "✅ Safe"}"
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@SensorActivity, "Firebase Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}