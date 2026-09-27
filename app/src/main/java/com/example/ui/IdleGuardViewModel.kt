package com.example.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.DailyStatEntity
import com.example.data.FocusSessionEntity
import com.example.data.IdleGuardRepository
import com.example.data.NudgeEventEntity
import com.example.data.TaskEntity
import com.example.preferences.PreferencesManager
import com.example.preferences.UserPreferences
import com.example.service.IdleTrackingService
import com.example.tracker.TrackerManager
import com.example.tracker.TrackerSnapshot
import com.example.ui.screens.WeekDay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppTab(val label: String) {
    TASKS("Tasks"),
    HOME("Home"),
    SETTINGS("Settings")
}

class IdleGuardViewModel(
    private val repository: IdleGuardRepository,
    private val preferencesManager: PreferencesManager,
    private val trackerManager: TrackerManager
) : ViewModel() {

    val trackerSnapshot: StateFlow<TrackerSnapshot> = trackerManager.snapshot
    val preferences: StateFlow<UserPreferences> = preferencesManager.userPreferences

    // Navigation state (Default is HOME with the 15-minute timer)
    private val _currentTab = MutableStateFlow(AppTab.HOME)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    // Tasks State
    val allTasks: StateFlow<List<TaskEntity>> = repository.allTasks
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _selectedDay = MutableStateFlow(WeekDay.getCurrentDay())
    val selectedDay: StateFlow<WeekDay> = _selectedDay.asStateFlow()

    fun selectTab(tab: AppTab) {
        _currentTab.value = tab
    }

    fun selectDay(day: WeekDay) {
        _selectedDay.value = day
    }

    fun addTask(title: String) {
        viewModelScope.launch {
            repository.addTask(
                dayOfWeek = _selectedDay.value.id,
                title = title
            )
        }
    }

    fun toggleTaskCompleted(taskId: Long, isCompleted: Boolean) {
        viewModelScope.launch {
            repository.setTaskCompleted(taskId, isCompleted)
        }
    }

    fun updateTaskTitle(taskId: Long, title: String) {
        viewModelScope.launch {
            repository.updateTaskTitle(taskId, title)
        }
    }

    fun deleteTask(taskId: Long) {
        viewModelScope.launch {
            repository.deleteTask(taskId)
        }
    }

    fun updateRepeatInterval(minutes: Int) {
        preferencesManager.updateRepeatInterval(minutes)
    }

    fun updateColorTheme(themeId: String) {
        preferencesManager.updateColorTheme(themeId)
    }

    fun onUserInteraction() {
        trackerManager.recordUserInteraction()
    }

    fun snooze(minutes: Int) {
        trackerManager.snooze(minutes)
    }

    fun acknowledgeNudge() {
        trackerManager.acknowledgeNudge()
    }

    fun dismissTriggerDialog() {
        trackerManager.dismissTriggerDialog()
    }

    fun toggleTimer() {
        trackerManager.toggleTimer()
    }

    fun startTimer() {
        trackerManager.startTimer()
    }

    fun pauseTimer() {
        trackerManager.pauseTimer()
    }

    fun resetTimer() {
        trackerManager.resetTimer()
    }

    fun setTimerDuration(hours: Int, minutes: Int, seconds: Int) {
        trackerManager.setTimerDuration(hours, minutes, seconds)
    }

    fun snoozeTimer(minutes: Int) {
        trackerManager.snoozeTimer(minutes)
    }

    fun testTriggerNow() {
        trackerManager.testTriggerNow()
    }

    class Factory(
        private val repository: IdleGuardRepository,
        private val preferencesManager: PreferencesManager,
        private val trackerManager: TrackerManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(IdleGuardViewModel::class.java)) {
                return IdleGuardViewModel(repository, preferencesManager, trackerManager) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
