package com.alboteanu.mybackgroundwebviewapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import android.app.PendingIntent

class KeepAliveService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()

        // 1. Creăm un Intent care arată către MainActivity
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            // Aceste flag-uri se asigură că nu deschidem o copie NOUĂ a aplicației,
            // ci o aducem în față pe cea deja existentă
            this.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        // 2. Împachetăm Intent-ul într-un PendingIntent (obligatoriu pentru notificări)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, "webview_channel")
            .setContentTitle("YouTube rulează în fundal")
            .setContentText("Atinge pentru a deschide aplicația")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent) // <-- 3. AICI LEGAM INTENT-UL DE NOTIFICARE
            .build()

        startForeground(1, notification)

        return START_STICKY
    }

    private fun createNotificationChannel() {
            // 1. Definim detaliile canalului
            val channelId = "webview_channel" // Trebuie să fie EXACT același ID pe care îl folosești la NotificationCompat.Builder
            val channelName = "Background Playback" // Numele pe care îl vede utilizatorul în setări
            val importance = NotificationManager.IMPORTANCE_LOW // LOW înseamnă că nu va face zgomot/vibrație când apare

            // 2. Creăm obiectul canalului
            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = "Menține redarea activă în fundal"
            }

            // 3. Înregistrăm canalul în sistemul Android
            val notificationManager: NotificationManager =
                getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
    }

    override fun onBind(intent: Intent?): IBinder? {
        // Returnăm null pentru că nu permitem altor componente
        // să se "lege" (bind) de acest serviciu.
        return null
    }
}