package com.example.securepoint

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val emailText = findViewById<TextView>(R.id.tv_email)
        val uidText = findViewById<TextView>(R.id.tv_uid)
        val roleText = findViewById<TextView>(R.id.tv_role)

        val user = FirebaseAuth.getInstance().currentUser
        val role = intent.getStringExtra("userRole") ?: "guest"

        emailText.text = "Email: ${user?.email ?: "N/A"}"
        uidText.text = "UID: ${user?.uid ?: "N/A"}"
        roleText.text = "Role: $role"
    }
}
