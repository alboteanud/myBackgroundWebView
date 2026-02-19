package com.alboteanu.mybackgroundwebviewapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat

class KeepAliveService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()

        val notification = NotificationCompat.Builder(this, "webview_channel")
            .setContentTitle("Aplicația rulează în fundal")
            .setContentText("Redare media activă...")
            .setSmallIcon(android.R.drawable.ic_media_play) // Pune un icon din resursele tale
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        // Pornește serviciul în prim-plan
        startForeground(1, notification)

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            "webview_channel",
            "Background Playback",
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }
}