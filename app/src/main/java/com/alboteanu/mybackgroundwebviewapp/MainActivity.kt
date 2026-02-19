package com.alboteanu.mybackgroundwebviewapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    private lateinit var webView: WebView
    private val NOTIFICATION_PERMISSION_CODE = 123

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Containerul de bază care rezolvă ceasul (Edge-to-Edge)
        val rootView = FrameLayout(this).apply {
            setBackgroundColor(android.graphics.Color.BLACK)
            fitsSystemWindows = true
        }

        // 2. CREĂM UN WEBVIEW "HACKUIT"
        webView = object : WebView(this) {
            override fun onWindowVisibilityChanged(visibility: Int) {
                // Indiferent ce zice sistemul, noi mințim Chromium că aplicația e mereu pe ecran (VISIBLE).
                // Astfel, decodorul video nu va fi închis niciodată când dai Home!
                super.onWindowVisibilityChanged(View.VISIBLE)
            }
        }.apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }

        rootView.addView(webView)
        setContentView(rootView)

        setupWebView()
        webView.loadUrl("https://m.youtube.com")
        checkNotificationPermission()
    }

    private fun checkNotificationPermission() {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), NOTIFICATION_PERMISSION_CODE
            )
        }
    }

    private fun setupWebView() {
        webView.webChromeClient = WebChromeClient()
        webView.webViewClient = WebViewClient()
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            mediaPlaybackRequiresUserGesture = false
            userAgentString =
                "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Mobile Safari/537.36"
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        val serviceIntent = Intent(this, KeepAliveService::class.java)
        ContextCompat.startForegroundService(this, serviceIntent)
    }

    override fun onResume() {
        super.onResume()
        stopService(Intent(this, KeepAliveService::class.java))
    }

    override fun onDestroy() {
        stopService(Intent(this, KeepAliveService::class.java))
        webView.destroy()
        super.onDestroy()
    }
}