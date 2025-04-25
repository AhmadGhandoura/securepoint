package com.example.securepoint

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onStart() {
        super.onStart()

        auth = FirebaseAuth.getInstance()

        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val dbRef = FirebaseDatabase.getInstance().getReference("users").child(userId)

            dbRef.get().addOnSuccessListener { snapshot ->
                val role = snapshot.child("role").getValue(String::class.java) ?: "guest"

                val intent = Intent(this, HomeActivity::class.java)
                intent.putExtra("userRole", role)
                startActivity(intent)
                finish()
            }.addOnFailureListener {
                Toast.makeText(this, "Auto-login failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_activity)

        FirebaseApp.initializeApp(this)
        auth = FirebaseAuth.getInstance()

        val emailEditText = findViewById<EditText>(R.id.et_email)
        val passwordEditText = findViewById<EditText>(R.id.et_password)
        val loginButton = findViewById<Button>(R.id.btn_login)
        val signUpTextView = findViewById<TextView>(R.id.tv_signup)
        val phoneAuthBtn = findViewById<Button>(R.id.btn_phone_auth)

        phoneAuthBtn.setOnClickListener {
            startActivity(Intent(this, PhoneAuthActivity::class.java))
        }

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter both email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val userId = auth.currentUser?.uid
                        if (userId != null) {
                            val dbRef = FirebaseDatabase.getInstance().getReference("users").child(userId)
                            dbRef.get().addOnSuccessListener { snapshot ->
                                val role = snapshot.child("role").getValue(String::class.java) ?: "guest"

                                val intent = Intent(this, HomeActivity::class.java)
                                intent.putExtra("userRole", role)
                                startActivity(intent)
                                finish()
                            }.addOnFailureListener {
                                Toast.makeText(this, "Failed to get user role", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        Toast.makeText(this, "Login failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        signUpTextView.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }
    }
}
