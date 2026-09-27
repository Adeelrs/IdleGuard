package com.example.tracker

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.example.data.IdleGuardRepository
import com.example.preferences.PreferencesManager
import com.example.service.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class TrackerState {
    ACTIVE,
    IDLE,
    SNOOZED,
    FOCUSING
}

data class TrackerSnapshot(
    val state: TrackerState = TrackerState.ACTIVE,
    val continuousActiveSeconds: Long = 0L,
    val secondsUntilNextTrigger: Long = 15 * 60L,
    val repeatIntervalMinutes: Int = 15,
    val isTriggerDialogOpen: Boolean = false,
    val snoozeSecondsRemaining: Long = 0L,
    val snoozeTotalMinutes: Int = 5,
    val focusSecondsRemaining: Long = 0L,
    val focusTotalMinutes: Int = 25,
    // Always-present focus timer state
    val timerTotalSeconds: Long = 15 * 60L,
    val timerRemainingSeconds: Long = 15 * 60L,
    val isTimerRunning: Boolean = false,
    val timerOffSeconds: Long = 0L,
    val todayDate: String = "",
    val todayScreenTimeSeconds: Long = 0L,
    val todayIdleTimeSeconds: Long = 0L,
    val todayFocusTimeSeconds: Long = 0L,
    val isAutoTracking: Boolean = true
)

class TrackerManager(
    private val context: Context,
    private val repository: IdleGuardRepository,
    private val preferencesManager: PreferencesManager
) {
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private val notificationHelper = NotificationHelper(context)
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    private val _snapshot = MutableStateFlow(
        TrackerSnapshot(
            todayDate = getTodayDateStr(),
            repeatIntervalMinutes = preferencesManager.userPreferences.value.repeatIntervalMinutes
        )
    )
    val snapshot: StateFlow<TrackerSnapshot> = _snapshot.asStateFlow()

    private var tickerJob: Job? = null
    private var lastInteractionTime = System.currentTimeMillis()

    init {
        // Collect preferences changes
        scope.launch {
            preferencesManager.userPreferences.collect { prefs ->
                _snapshot.value = _snapshot.value.copy(
                    repeatIntervalMinutes = prefs.repeatIntervalMinutes
                )
            }
        }

        // Collect today's stats from Room
        scope.launch {
            repository.getTodayStats(getTodayDateStr()).collect { stat ->
                if (stat != null) {
                    _snapshot.value = _snapshot.value.copy(
                        todayScreenTimeSeconds = stat.screenTimeSeconds,
                        todayIdleTimeSeconds = stat.idleTimeSeconds,
                        todayFocusTimeSeconds = stat.focusTimeSeconds
                    )
                }
            }
        }

        startTrackingLoop()
    }

    fun getTodayDateStr(): String = dateFormat.format(Date())

    private fun startTrackingLoop() {
        tickerJob?.cancel()
        tickerJob = scope.launch(Dispatchers.Default) {
            while (isActive) {
                delay(1000L)
                processTick()
            }
        }
    }

    private suspend fun processTick() {
        val today = getTodayDateStr()
        val currentSnap = _snapshot.value
        val prefs = preferencesManager.userPreferences.value

        // Check if day changed
        if (today != currentSnap.todayDate) {
            _snapshot.value = _snapshot.value.copy(
                todayDate = today,
                continuousActiveSeconds = 0L
            )
        }

        // 1. Process Focus Timer (Always Present)
        var newTimerRemaining = currentSnap.timerRemainingSeconds
        var newIsRunning = currentSnap.isTimerRunning
        var newTimerOffSeconds = currentSnap.timerOffSeconds
        var newDialogOpen = currentSnap.isTriggerDialogOpen

        if (newIsRunning) {
            newTimerOffSeconds = 0L // reset off counter
            newTimerRemaining -= 1
            repository.recordFocusTime(today, 1L)

            if (newTimerRemaining <= 0) {
                // Timer finished!
                newIsRunning = false
                newTimerRemaining = currentSnap.timerTotalSeconds
                newDialogOpen = true

                // Alert and notification
                vibrateFeedback(longArrayOf(0, 400, 200, 400))
                notificationHelper.showTimerCompleteAlert(currentSnap.timerTotalSeconds / 60)
            }
        } else {
            // Timer is off: increment off-seconds
            newTimerOffSeconds += 1L

            // Hourly reminder when timer is not set on! (3600 seconds = 1 hour)
            if (newTimerOffSeconds > 0 && newTimerOffSeconds % 3600L == 0L) {
                val hoursOff = newTimerOffSeconds / 3600L
                notificationHelper.showHourlyRefocusReminder(hoursOff)
                vibrateFeedback(longArrayOf(0, 250, 150, 250))
            }
        }

        // 2. Track screen and idle activity
        val newActiveSec = currentSnap.continuousActiveSeconds + 1
        val intervalSec = prefs.repeatIntervalMinutes * 60L
        val remainingSec = (intervalSec - (newActiveSec % intervalSec)).let {
            if (it == intervalSec && newActiveSec > 0) 0L else it
        }

        repository.recordScreenTime(today, 1L)

        // Trigger nudge if interval reached
        if (prefs.isNudgeEnabled && newActiveSec > 0 && newActiveSec % intervalSec == 0L) {
            newDialogOpen = true
            vibrateFeedback(longArrayOf(0, 300, 200, 300))
        }

        _snapshot.value = currentSnap.copy(
            continuousActiveSeconds = newActiveSec,
            secondsUntilNextTrigger = if (remainingSec == 0L) intervalSec else remainingSec,
            timerRemainingSeconds = newTimerRemaining,
            isTimerRunning = newIsRunning,
            timerOffSeconds = newTimerOffSeconds,
            isTriggerDialogOpen = newDialogOpen
        )
    }

    fun startTimer() {
        _snapshot.value = _snapshot.value.copy(
            isTimerRunning = true,
            timerOffSeconds = 0L
        )
    }

    fun pauseTimer() {
        _snapshot.value = _snapshot.value.copy(
            isTimerRunning = false
        )
    }

    fun toggleTimer() {
        val nextRunning = !_snapshot.value.isTimerRunning
        _snapshot.value = _snapshot.value.copy(
            isTimerRunning = nextRunning,
            timerOffSeconds = if (nextRunning) 0L else _snapshot.value.timerOffSeconds
        )
    }

    fun resetTimer() {
        _snapshot.value = _snapshot.value.copy(
            timerRemainingSeconds = _snapshot.value.timerTotalSeconds,
            isTimerRunning = false
        )
    }

    fun setTimerDuration(hours: Int, minutes: Int, seconds: Int) {
        val total = (hours * 3600L) + (minutes * 60L) + seconds
        val validTotal = if (total <= 0) 15 * 60L else total
        _snapshot.value = _snapshot.value.copy(
            timerTotalSeconds = validTotal,
            timerRemainingSeconds = validTotal,
            isTimerRunning = false
        )
    }

    fun snoozeTimer(additionalMinutes: Int) {
        val currentRemaining = _snapshot.value.timerRemainingSeconds
        val newRemaining = currentRemaining + (additionalMinutes * 60L)
        val newTotal = maxOf(_snapshot.value.timerTotalSeconds, newRemaining)
        _snapshot.value = _snapshot.value.copy(
            timerTotalSeconds = newTotal,
            timerRemainingSeconds = newRemaining,
            isTimerRunning = true,
            timerOffSeconds = 0L,
            isTriggerDialogOpen = false
        )
    }

    fun recordUserInteraction() {
        lastInteractionTime = System.currentTimeMillis()
    }

    fun toggleIdleState(isIdle: Boolean) {
        _snapshot.value = _snapshot.value.copy(
            state = if (isIdle) TrackerState.IDLE else TrackerState.ACTIVE
        )
    }

    fun snooze(minutes: Int) {
        snoozeTimer(minutes)
    }

    fun acknowledgeNudge() {
        _snapshot.value = _snapshot.value.copy(
            isTriggerDialogOpen = false
        )
        notificationHelper.cancelNudgeNotification()
    }

    fun startFocusSession(minutes: Int) {
        setTimerDuration(0, minutes, 0)
        startTimer()
    }

    fun cancelFocusSession() {
        pauseTimer()
    }

    fun dismissTriggerDialog() {
        _snapshot.value = _snapshot.value.copy(isTriggerDialogOpen = false)
        notificationHelper.cancelNudgeNotification()
    }

    fun testTriggerNow() {
        _snapshot.value = _snapshot.value.copy(isTriggerDialogOpen = true)
        vibrateFeedback(longArrayOf(0, 300, 200, 300))
    }

    private fun vibrateFeedback(pattern: LongArray) {
        if (!preferencesManager.userPreferences.value.vibrationEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager =
                    context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createWaveform(pattern, -1)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                vibrator?.vibrate(VibrationEffect.createWaveform(pattern, -1))
            }
        } catch (_: Exception) {
            // Ignore if vibration unavailable
        }
    }
}
