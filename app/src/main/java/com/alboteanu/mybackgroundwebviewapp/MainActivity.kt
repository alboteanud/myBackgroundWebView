package com.alboteanu.mybackgroundwebviewapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    private lateinit var webView: WebView
    private val NOTIFICATION_PERMISSION_CODE = 123

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        webView = WebView(this)
        setContentView(webView)
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
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            mediaPlaybackRequiresUserGesture = false
            userAgentString =
                "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Mobile Safari/537.36"
        }
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                val js = """
            javascript:(function() {
                Object.defineProperty(document, 'visibilityState', {
                    get: function() { return 'visible'; }
                });
                Object.defineProperty(document, 'hidden', {
                    get: function() { return false; }
                });
                document.addEventListener('visibilitychange', function(e) {
                    e.stopImmediatePropagation();
                }, true);
                setInterval(function() {
                    var v = document.querySelector('video');
                    if(v && v.paused) { v.play(); }
                }, 2000);
            })();
        """.trimIndent()
                view?.evaluateJavascript(js, null)
            }
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