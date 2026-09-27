package com.example

import android.app.Application
import com.example.data.AppDatabase
import com.example.data.IdleGuardRepository
import com.example.preferences.PreferencesManager
import com.example.tracker.TrackerManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class IdleGuardApplication : Application() {
    lateinit var database: AppDatabase
        private set

    lateinit var repository: IdleGuardRepository
        private set

    lateinit var preferencesManager: PreferencesManager
        private set

    lateinit var trackerManager: TrackerManager
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        database = AppDatabase.getInstance(this)
        repository = IdleGuardRepository(
            dailyStatDao = database.dailyStatDao(),
            nudgeEventDao = database.nudgeEventDao(),
            focusSessionDao = database.focusSessionDao(),
            taskDao = database.taskDao()
        )
        preferencesManager = PreferencesManager(this)
        trackerManager = TrackerManager(this, repository, preferencesManager)

        // Seed initial weekly tasks if empty
        CoroutineScope(Dispatchers.IO).launch {
            repository.ensureInitialTasksSeeded()
        }
    }

    companion object {
        lateinit var instance: IdleGuardApplication
            private set
    }
}
