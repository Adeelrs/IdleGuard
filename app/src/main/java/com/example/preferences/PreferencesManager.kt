package com.example.preferences

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("idleguard_prefs", Context.MODE_PRIVATE)

    private val _userPreferences = MutableStateFlow(loadPreferences())
    val userPreferences: StateFlow<UserPreferences> = _userPreferences.asStateFlow()

    private fun loadPreferences(): UserPreferences {
        return UserPreferences(
            repeatIntervalMinutes = prefs.getInt(KEY_REPEAT_INTERVAL, 15),
            colorTheme = prefs.getString(KEY_COLOR_THEME, AppColorTheme.EMERALD.id) ?: AppColorTheme.EMERALD.id,
            isNudgeEnabled = prefs.getBoolean(KEY_NUDGE_ENABLED, true),
            defaultSnoozeMinutes = prefs.getInt(KEY_DEFAULT_SNOOZE, 5),
            dailyScreenTimeGoalMinutes = prefs.getInt(KEY_SCREEN_TIME_GOAL, 120),
            dailyFocusGoalMinutes = prefs.getInt(KEY_FOCUS_GOAL, 60),
            vibrationEnabled = prefs.getBoolean(KEY_VIBRATION, true),
            soundEnabled = prefs.getBoolean(KEY_SOUND, true),
            idleAutoDetectMinutes = prefs.getInt(KEY_IDLE_DETECT, 3),
            persistentServiceEnabled = prefs.getBoolean(KEY_PERSISTENT_SERVICE, true)
        )
    }

    fun updateRepeatInterval(minutes: Int) {
        prefs.edit().putInt(KEY_REPEAT_INTERVAL, minutes).apply()
        _userPreferences.value = _userPreferences.value.copy(repeatIntervalMinutes = minutes)
    }

    fun updateColorTheme(themeId: String) {
        prefs.edit().putString(KEY_COLOR_THEME, themeId).apply()
        _userPreferences.value = _userPreferences.value.copy(colorTheme = themeId)
    }

    fun toggleNudgeEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NUDGE_ENABLED, enabled).apply()
        _userPreferences.value = _userPreferences.value.copy(isNudgeEnabled = enabled)
    }

    fun updateDefaultSnooze(minutes: Int) {
        prefs.edit().putInt(KEY_DEFAULT_SNOOZE, minutes).apply()
        _userPreferences.value = _userPreferences.value.copy(defaultSnoozeMinutes = minutes)
    }

    fun updateDailyScreenTimeGoal(minutes: Int) {
        prefs.edit().putInt(KEY_SCREEN_TIME_GOAL, minutes).apply()
        _userPreferences.value = _userPreferences.value.copy(dailyScreenTimeGoalMinutes = minutes)
    }

    fun updateDailyFocusGoal(minutes: Int) {
        prefs.edit().putInt(KEY_FOCUS_GOAL, minutes).apply()
        _userPreferences.value = _userPreferences.value.copy(dailyFocusGoalMinutes = minutes)
    }

    fun toggleVibration(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_VIBRATION, enabled).apply()
        _userPreferences.value = _userPreferences.value.copy(vibrationEnabled = enabled)
    }

    fun toggleSound(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SOUND, enabled).apply()
        _userPreferences.value = _userPreferences.value.copy(soundEnabled = enabled)
    }

    fun togglePersistentService(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_PERSISTENT_SERVICE, enabled).apply()
        _userPreferences.value = _userPreferences.value.copy(persistentServiceEnabled = enabled)
    }

    companion object {
        private const val KEY_REPEAT_INTERVAL = "repeat_interval"
        private const val KEY_COLOR_THEME = "color_theme"
        private const val KEY_NUDGE_ENABLED = "nudge_enabled"
        private const val KEY_DEFAULT_SNOOZE = "default_snooze"
        private const val KEY_SCREEN_TIME_GOAL = "screen_time_goal"
        private const val KEY_FOCUS_GOAL = "focus_goal"
        private const val KEY_VIBRATION = "vibration_enabled"
        private const val KEY_SOUND = "sound_enabled"
        private const val KEY_IDLE_DETECT = "idle_detect"
        private const val KEY_PERSISTENT_SERVICE = "persistent_service"
    }
}
