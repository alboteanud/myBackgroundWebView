package com.alboteanu.mybackgroundwebviewapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat

class KeepAliveService : Service() {

    // Adăugăm variabila pentru WakeLock
    private var wakeLock: PowerManager.WakeLock? = null
    private val CHANNEL_ID = "webview_channel"

    override fun onCreate() {
        super.onCreate()

        // 1. ACHIZIȚIONĂM WAKELOCK-UL
        // Asta previne intrarea procesorului în Deep Sleep când ecranul e stins
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "MyApp::WebViewBackgroundAudio"
        )
        // Setăm un timeout de siguranță de 2 ore pentru a nu consuma bateria la infinit dacă aplicația "crapă"
        wakeLock?.acquire(2 * 60 * 60 * 1000L)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()

        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            this.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("YouTube rulează în fundal")
            .setContentText("Atinge pentru a deschide aplicația")
            .setSmallIcon(android.R.drawable.ic_media_play) // Asigură-te că ai iconița asta sau pune-o pe a ta
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .build()

        startForeground(
            1,
            notification,
            ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
        )

        return START_STICKY
    }

    override fun onDestroy() {
        // 3. ELIBERĂM WAKELOCK-UL OBLIGATORIU
        // Dacă nu facem asta, telefonul utilizatorului nu va mai intra niciodată în sleep și bateria se va descărca rapid
        wakeLock?.let {
            if (it.isHeld) {
                it.release()
            }
        }
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Background Playback",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Menține redarea activă în fundal"
        }

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}