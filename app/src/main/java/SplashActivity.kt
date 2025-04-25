package com.example.securepoint

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class SplashActivity : AppCompatActivity() {
    private val SPLASH_DURATION = 2500L // 2.5 seconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler(Looper.getMainLooper()).postDelayed({
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                val dbRef = FirebaseDatabase.getInstance().getReference("users").child(user.uid)
                dbRef.get().addOnSuccessListener { snapshot ->
                    val role = snapshot.child("role").getValue(String::class.java) ?: "guest"
                    val intent = Intent(this, HomeActivity::class.java)
                    intent.putExtra("userRole", role)
                    startActivity(intent)
                    finish()
                }.addOnFailureListener {
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
            } else {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }, SPLASH_DURATION)
    }
}
