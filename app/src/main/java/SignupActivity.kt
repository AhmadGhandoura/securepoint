package com.example.securepoint

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class SignupActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signup_activity)

        auth = FirebaseAuth.getInstance()

        val emailEditText = findViewById<EditText>(R.id.et_email)
        val passwordEditText = findViewById<EditText>(R.id.et_password)
        val signUpButton = findViewById<Button>(R.id.btn_signup)

        signUpButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email and password required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val userId = auth.currentUser!!.uid
                        val userRef = FirebaseDatabase.getInstance().getReference("users")

                        // Check if this is the first user
                        userRef.get().addOnSuccessListener { snapshot ->
                            val role = if (snapshot.childrenCount == 0L) "admin" else "guest"

                            val userData = mapOf(
                                "email" to email,
                                "role" to role
                            )

                            userRef.child(userId).setValue(userData).addOnSuccessListener {
                                Toast.makeText(this, "Signed up as $role ✅", Toast.LENGTH_SHORT).show()
                                val intent = Intent(this, HomeActivity::class.java)
                                intent.putExtra("userRole", role)
                                startActivity(intent)
                                finish()
                            }.addOnFailureListener {
                                Toast.makeText(this, "Failed to save user data", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        Toast.makeText(this, "Signup failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    // 👉 "Already have account? Login" button handler
    fun goToLogin(view: View) {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
    fun goToPhone(view: View) {
        val intent = Intent(this, PhoneAuthActivity::class.java)
        startActivity(intent)
        finish()
    }
}
