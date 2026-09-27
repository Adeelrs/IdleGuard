package com.example.service

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import com.example.IdleGuardApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class IdleTrackingService : Service() {
    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private lateinit var notificationHelper: NotificationHelper

    private val screenReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val tracker = IdleGuardApplication.instance.trackerManager
            when (intent?.action) {
                Intent.ACTION_SCREEN_OFF -> {
                    tracker.toggleIdleState(isIdle = true)
                }
                Intent.ACTION_SCREEN_ON, Intent.ACTION_USER_PRESENT -> {
                    tracker.toggleIdleState(isIdle = false)
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        notificationHelper = NotificationHelper(this)

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_USER_PRESENT)
        }
        registerReceiver(screenReceiver, filter)

        val trackerManager = IdleGuardApplication.instance.trackerManager
        val initialSnap = trackerManager.snapshot.value

        // Start foreground with initial lock-screen visible timer notification
        val initialNotif = notificationHelper.buildTimerNotification(
            totalSeconds = initialSnap.timerTotalSeconds,
            remainingSeconds = initialSnap.timerRemainingSeconds,
            isRunning = initialSnap.isTimerRunning
        )
        startForeground(NotificationHelper.NOTIFICATION_ID_TRACKING, initialNotif)

        // Observe snapshot updates and refresh the ongoing timer notification
        serviceScope.launch {
            trackerManager.snapshot.collect { snapshot ->
                val notif = notificationHelper.buildTimerNotification(
                    totalSeconds = snapshot.timerTotalSeconds,
                    remainingSeconds = snapshot.timerRemainingSeconds,
                    isRunning = snapshot.isTimerRunning
                )
                val manager = getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
                manager.notify(NotificationHelper.NOTIFICATION_ID_TRACKING, notif)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(screenReceiver)
        } catch (_: Exception) {}
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, IdleTrackingService::class.java)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, IdleTrackingService::class.java)
            context.stopService(intent)
        }
    }
}
