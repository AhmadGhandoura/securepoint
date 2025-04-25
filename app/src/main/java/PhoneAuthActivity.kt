package com.example.securepoint
import android.content.Intent

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import java.util.concurrent.TimeUnit

class PhoneAuthActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var storedVerificationId: String
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phone_auth)

        auth = FirebaseAuth.getInstance()

        val etPhone = findViewById<EditText>(R.id.etPhone)
        val etOTP = findViewById<EditText>(R.id.etOTP)
        val btnSendCode = findViewById<Button>(R.id.btnSendCode)
        val btnVerify = findViewById<Button>(R.id.btnVerify)

        btnSendCode.setOnClickListener {
            val phone = etPhone.text.toString().trim()
            if (phone.isNotEmpty()) {
                sendVerificationCode("+$phone") // include country code!
            } else {
                Toast.makeText(this, "Enter phone number", Toast.LENGTH_SHORT).show()
            }
        }

        btnVerify.setOnClickListener {
            val code = etOTP.text.toString().trim()
            if (code.isNotEmpty()) {
                verifyCode(code)
            }
        }
    }

    private fun sendVerificationCode(phoneNumber: String) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            signInWithCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            Toast.makeText(this@PhoneAuthActivity, "Failed: ${e.message}", Toast.LENGTH_LONG).show()
        }

        override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
            super.onCodeSent(verificationId, token)
            storedVerificationId = verificationId
            resendToken = token
            Toast.makeText(this@PhoneAuthActivity, "OTP Sent", Toast.LENGTH_SHORT).show()
        }
    }

    private fun verifyCode(code: String) {
        val credential = PhoneAuthProvider.getCredential(storedVerificationId, code)
        signInWithCredential(credential)
    }

    private fun signInWithCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnSuccessListener {
                Toast.makeText(this, "Phone Auth Success!", Toast.LENGTH_SHORT).show()
                // Optional: Go to home screen
                startActivity(Intent(this, HomeActivity::class.java))
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Auth Failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
