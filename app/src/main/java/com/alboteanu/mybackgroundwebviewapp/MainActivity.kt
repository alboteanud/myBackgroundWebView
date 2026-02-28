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

import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

class MainActivity : AppCompatActivity() {
    private lateinit var webView: WebView
    private val NOTIFICATION_PERMISSION_CODE = 123
    private lateinit var rootView: FrameLayout
    private var customView: View? = null
    private var customViewCallback: WebChromeClient.CustomViewCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Containerul de bază care rezolvă ceasul (Edge-to-Edge)
        rootView = FrameLayout(this).apply {
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

    private fun setupWebView_0() {
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



    private fun setupWebView() {
        webView.webViewClient = WebViewClient()

        // Custom WebChromeClient for Full-Screen Video
        webView.webChromeClient = object : WebChromeClient() {

            // Triggered when the full-screen button is tapped
            override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                super.onShowCustomView(view, callback)

                // If a view already exists, ignore this new request
                if (customView != null) {
                    callback?.onCustomViewHidden()
                    return
                }

                customView = view
                customViewCallback = callback

                // 1. Hide the normal WebView
                webView.visibility = View.GONE

                // 2. Add the custom video view to our root FrameLayout
                rootView.addView(
                    customView, FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                    )
                )

                // 3. (Optional but recommended) Hide system bars for a true full-screen experience
                val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
                windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
            }

            // Triggered when exiting full-screen (e.g., pressing the minimize button or back button)
            override fun onHideCustomView() {
                super.onHideCustomView()

                if (customView == null) return

                // 1. Remove the custom video view
                rootView.removeView(customView)
                customView = null

                // 2. Restore the normal WebView
                webView.visibility = View.VISIBLE

                // 3. Notify the web page that full-screen is over
                customViewCallback?.onCustomViewHidden()
                customViewCallback = null

                // 4. Restore system bars
                val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
                windowInsetsController.show(WindowInsetsCompat.Type.systemBars())
            }
        }

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

    override fun onBackPressed() {
        // If we are in full screen, exit full screen first
        if (customView != null) {
            webView.webChromeClient?.onHideCustomView()
        } else if (webView.canGoBack()) {
            // If WebView can go back a page, do that
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

}