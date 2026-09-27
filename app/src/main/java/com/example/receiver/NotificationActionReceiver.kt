package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.IdleGuardApplication

class NotificationActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val app = IdleGuardApplication.instance
        when (intent?.action) {
            ACTION_START_TIMER -> {
                app.trackerManager.startTimer()
            }
            ACTION_PAUSE_TIMER -> {
                app.trackerManager.pauseTimer()
            }
            ACTION_RESET_TIMER -> {
                app.trackerManager.resetTimer()
            }
            ACTION_SNOOZE_5M -> {
                app.trackerManager.snoozeTimer(5)
            }
            ACTION_ACKNOWLEDGE -> {
                app.trackerManager.acknowledgeNudge()
            }
        }
    }

    companion object {
        const val ACTION_START_TIMER = "com.example.idleguard.ACTION_START_TIMER"
        const val ACTION_PAUSE_TIMER = "com.example.idleguard.ACTION_PAUSE_TIMER"
        const val ACTION_RESET_TIMER = "com.example.idleguard.ACTION_RESET_TIMER"
        const val ACTION_SNOOZE_5M = "com.example.idleguard.ACTION_SNOOZE_5M"
        const val ACTION_ACKNOWLEDGE = "com.example.idleguard.ACTION_ACKNOWLEDGE"
    }
}
