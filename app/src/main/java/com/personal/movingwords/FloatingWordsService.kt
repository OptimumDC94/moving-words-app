/package com.personal.movingwords

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.graphics.drawable.Icon
import android.hardware.input.InputManager
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager

class FloatingWordsService : Service() {

    companion object {
        private const val CHANNEL_ID = "floating_words_channel"
        private const val NOTIFICATION_ID = 1001
        const val ACTION_STOP = "com.personal.movingwords.ACTION_STOP"
    }

    private var windowManager: WindowManager? = null
    private var overlayView: MovingWordsView? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }

        startAsForeground()
        showOrRefreshOverlay()
        return START_STICKY
    }

    private fun startAsForeground() {
        val manager = getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Floating Words",
            NotificationManager.IMPORTANCE_LOW
        )
        manager.createNotificationChannel(channel)

        val stopIntent = Intent(this, FloatingWordsService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            0,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val openIntent = Intent(this, MainActivity::class.java)
        val openPendingIntent = PendingIntent.getActivity(
            this,
            1,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIcon = Icon.createWithResource(this, android.R.drawable.ic_menu_close_clear_cancel)
        val stopAction = Notification.Action.Builder(stopIcon, "Stop", stopPendingIntent).build()

        val notification = Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("Moving Words Active")
            .setContentText("Your words are floating over all apps")
            .setSmallIcon(android.R.drawable.ic_menu_edit)
            .setContentIntent(openPendingIntent)
            .addAction(stopAction)
            .setOngoing(true)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun showOrRefreshOverlay() {
        if (windowManager == null) {
            windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        }

        overlayView?.let { windowManager?.removeView(it) }

        val words = WordStorage.loadWords(this)
        val view = MovingWordsView(this, words, transparentBackground = true)

        val safeAlpha = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val inputManager = getSystemService(InputManager::class.java)
            ((inputManager?.maximumObscuringOpacityForTouch ?: 0.8f) - 0.05f).coerceIn(0.2f, 0.8f)
        } else {
            0.8f
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            alpha = safeAlpha
        }

        windowManager?.addView(view, params)
        overlayView = view
    }

    override fun onDestroy() {
        overlayView?.let {
            windowManager?.removeView(it)
            overlayView = null
        }
        super.onDestroy()
    }
}