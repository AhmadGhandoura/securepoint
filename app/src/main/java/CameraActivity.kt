package com.example.securepoint

import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity

class CameraActivity : AppCompatActivity() {

    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.camera_activity)

        webView = findViewById(R.id.webView)

        // Configure WebView
        val webSettings: WebSettings = webView.settings
        webSettings.javaScriptEnabled = true
        webSettings.loadWithOverviewMode = true
        webSettings.useWideViewPort = true

        // Set WebView client
        webView.webChromeClient = WebChromeClient()

        // Load ESP32 Camera Stream
        val esp32CamUrl = "http://192.168.1.100:81/stream" // Change this to your ESP32-CAM's IP
        webView.loadUrl(esp32CamUrl)
    }
}
