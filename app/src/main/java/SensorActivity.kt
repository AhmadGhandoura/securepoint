package com.example.securepoint

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SensorActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sensor_activity)

        val motionSensor = findViewById<TextView>(R.id.tv_motion_sensor)
        val temperatureSensor = findViewById<TextView>(R.id.tv_temperature_sensor)
        val refreshButton = findViewById<Button>(R.id.btn_refresh_sensors)

        refreshButton.setOnClickListener {
            // Simulate fetching sensor data
            motionSensor.text = "Motion Sensor: No movement detected"
            temperatureSensor.text = "Temperature: 22°C"
        }
    }
}
