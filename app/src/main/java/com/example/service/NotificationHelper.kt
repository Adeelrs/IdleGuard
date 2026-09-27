package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.receiver.NotificationActionReceiver
import java.util.Locale

class NotificationHelper(private val context: Context) {
    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Channel for the persistent ongoing timer notification
            val timerChannel = NotificationChannel(
                CHANNEL_TIMER_ONGOING,
                "Focus Timer (Always Present)",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Always-present notification showing live focus timer and lock screen controls"
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }

            // Channel for hourly refocus reminders when timer is off
            val reminderChannel = NotificationChannel(
                CHANNEL_HOURLY_REMINDERS,
                "Refocus Hourly Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminds you every hour when the focus timer is not set on"
                enableVibration(true)
                setShowBadge(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }

            // Channel for timer complete / nudge alerts
            val alertChannel = NotificationChannel(
                CHANNEL_TIMER_ALERTS,
                "Timer Complete & Snooze Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts when timer completes or repeat interval triggers"
                enableVibration(true)
                setShowBadge(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }

            notificationManager.createNotificationChannel(timerChannel)
            notificationManager.createNotificationChannel(reminderChannel)
            notificationManager.createNotificationChannel(alertChannel)
        }
    }

    /**
     * Builds the persistent notification displaying the timer.
     * Fully visible on the Lock Screen (Samsung One UI / S25 Ultra compatible).
     */
    fun buildTimerNotification(
        totalSeconds: Long,
        remainingSeconds: Long,
        isRunning: Boolean
    ): Notification {
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action: Start / Pause Toggle
        val toggleActionIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = if (isRunning) {
                NotificationActionReceiver.ACTION_PAUSE_TIMER
            } else {
                NotificationActionReceiver.ACTION_START_TIMER
            }
        }
        val togglePendingIntent = PendingIntent.getBroadcast(
            context,
            201,
            toggleActionIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action: Reset
        val resetIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_RESET_TIMER
        }
        val resetPendingIntent = PendingIntent.getBroadcast(
            context,
            202,
            resetIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action: +5m Snooze
        val snoozeIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_SNOOZE_5M
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            203,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val hours = remainingSeconds / 3600
        val mins = (remainingSeconds % 3600) / 60
        val secs = remainingSeconds % 60

        val formattedTime = if (hours > 0) {
            String.format(Locale.US, "%02d:%02d:%02d", hours, mins, secs)
        } else {
            String.format(Locale.US, "%02d:%02d", mins, secs)
        }

        val title = if (isRunning) {
            "⏱️ Focus Timer: $formattedTime"
        } else {
            "⏸️ Focus Timer: $formattedTime (Off)"
        }

        val contentText = if (isRunning) {
            "Focus session in progress • Tap to open"
        } else {
            "Timer is paused • Tap Start to get in focus mode"
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_TIMER_ONGOING)
            .setContentTitle(title)
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_STOPWATCH)
            // Critical for Samsung S25 Ultra lock screen visibility:
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .addAction(
                0,
                if (isRunning) "Pause" else "Start",
                togglePendingIntent
            )
            .addAction(0, "Reset", resetPendingIntent)

        if (isRunning) {
            builder.addAction(0, "+5m Snooze", snoozePendingIntent)
        }

        return builder.build()
    }

    /**
     * Hourly Refocus Reminder: Fired every hour when the timer is not running.
     * Fully visible on the Lock Screen.
     */
    fun showHourlyRefocusReminder(hoursOff: Long) {
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            301,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val startTimerIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_START_TIMER
        }
        val startPendingIntent = PendingIntent.getBroadcast(
            context,
            302,
            startTimerIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_HOURLY_REMINDERS)
            .setContentTitle("Get Back in Focus Mode! 🎯")
            .setContentText("Your timer has been off for ${hoursOff}h. Tap Start to begin focusing!")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(0, "Start 15m Focus", startPendingIntent)
            .build()

        notificationManager.notify(NOTIFICATION_ID_HOURLY_REMINDER, notification)
    }

    /**
     * Alert triggered when timer finishes.
     */
    fun showTimerCompleteAlert(totalMinutes: Long) {
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_OPEN_TRIGGER_DIALOG, true)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            401,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val snoozeIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_SNOOZE_5M
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            402,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_TIMER_ALERTS)
            .setContentTitle("🎉 Focus Session Finished!")
            .setContentText("Completed ${totalMinutes}m session. Great job! Snooze +5m or take a break.")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(0, "+5m Snooze", snoozePendingIntent)
            .build()

        notificationManager.notify(NOTIFICATION_ID_TIMER_COMPLETE, notification)
    }

    fun cancelNudgeNotification() {
        notificationManager.cancel(NOTIFICATION_ID_TIMER_COMPLETE)
    }

    companion object {
        const val CHANNEL_TIMER_ONGOING = "idleguard_timer_ongoing"
        const val CHANNEL_HOURLY_REMINDERS = "idleguard_hourly_reminders"
        const val CHANNEL_TIMER_ALERTS = "idleguard_timer_alerts"

        const val NOTIFICATION_ID_TRACKING = 1001
        const val NOTIFICATION_ID_HOURLY_REMINDER = 1002
        const val NOTIFICATION_ID_TIMER_COMPLETE = 1003

        const val EXTRA_OPEN_TRIGGER_DIALOG = "extra_open_trigger_dialog"
    }
}
