package com.example.data

import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class IdleGuardRepository(
    private val dailyStatDao: DailyStatDao,
    private val nudgeEventDao: NudgeEventDao,
    private val focusSessionDao: FocusSessionDao,
    private val taskDao: TaskDao
) {
    val recentStats: Flow<List<DailyStatEntity>> = dailyStatDao.getRecentStats(7)
    val allNudgeEvents: Flow<List<NudgeEventEntity>> = nudgeEventDao.getAllNudgeEvents()
    val allFocusSessions: Flow<List<FocusSessionEntity>> = focusSessionDao.getAllSessions()
    val allTasks: Flow<List<TaskEntity>> = taskDao.getAllTasks()

    fun getTasksForDay(dayOfWeek: String): Flow<List<TaskEntity>> {
        return taskDao.getTasksForDay(dayOfWeek.uppercase(Locale.getDefault()))
    }

    suspend fun addTask(dayOfWeek: String, title: String): Long {
        return taskDao.insertTask(
            TaskEntity(
                dayOfWeek = dayOfWeek.uppercase(Locale.getDefault()),
                title = title.trim(),
                isCompleted = false
            )
        )
    }

    suspend fun setTaskCompleted(taskId: Long, isCompleted: Boolean) {
        taskDao.updateCompletion(taskId, isCompleted)
    }

    suspend fun updateTaskTitle(taskId: Long, title: String) {
        taskDao.updateTitle(taskId, title.trim())
    }

    suspend fun deleteTask(taskId: Long) {
        taskDao.deleteTaskById(taskId)
    }

    suspend fun ensureInitialTasksSeeded() {
        if (taskDao.getTaskCount() == 0) {
            val sampleTasks = listOf(
                TaskEntity(dayOfWeek = "MONDAY", title = "Review weekly priorities & clean desk", isCompleted = true),
                TaskEntity(dayOfWeek = "MONDAY", title = "Deep work: complete main project milestone", isCompleted = false),
                TaskEntity(dayOfWeek = "MONDAY", title = "Limit phone screen time under 2 hours", isCompleted = false),

                TaskEntity(dayOfWeek = "TUESDAY", title = "Morning exercise & 10m mindful meditation", isCompleted = true),
                TaskEntity(dayOfWeek = "TUESDAY", title = "Draft project documentation & send update", isCompleted = false),
                TaskEntity(dayOfWeek = "TUESDAY", title = "Zero idle phone usage during work blocks", isCompleted = false),

                TaskEntity(dayOfWeek = "WEDNESDAY", title = "Mid-week focus sprint (2x 45m sessions)", isCompleted = false),
                TaskEntity(dayOfWeek = "WEDNESDAY", title = "Read 20 pages of current book", isCompleted = false),

                TaskEntity(dayOfWeek = "THURSDAY", title = "Organize inbox & clear backlog tasks", isCompleted = false),
                TaskEntity(dayOfWeek = "THURSDAY", title = "Evening offline walk without phone", isCompleted = false),

                TaskEntity(dayOfWeek = "FRIDAY", title = "Finalize weekly deliverables", isCompleted = false),
                TaskEntity(dayOfWeek = "FRIDAY", title = "Weekly reflection & productivity review", isCompleted = false),

                TaskEntity(dayOfWeek = "SATURDAY", title = "Outdoor activity / workout", isCompleted = false),
                TaskEntity(dayOfWeek = "SATURDAY", title = "Digital detox afternoon (2 hours unplugged)", isCompleted = false),

                TaskEntity(dayOfWeek = "SUNDAY", title = "Plan meals & schedule for upcoming week", isCompleted = false),
                TaskEntity(dayOfWeek = "SUNDAY", title = "Review weekly achievements & relax", isCompleted = false)
            )
            taskDao.insertTasks(sampleTasks)
        }
    }

    fun getTodayStats(date: String): Flow<DailyStatEntity?> {
        return dailyStatDao.getDailyStat(date)
    }

    suspend fun getTodayStatsSync(date: String): DailyStatEntity {
        return dailyStatDao.getDailyStatSync(date) ?: DailyStatEntity(date = date)
    }

    suspend fun recordScreenTime(date: String, secondsToAdd: Long) {
        val current = getTodayStatsSync(date)
        dailyStatDao.upsert(
            current.copy(
                screenTimeSeconds = current.screenTimeSeconds + secondsToAdd
            )
        )
    }

    suspend fun recordIdleTime(date: String, secondsToAdd: Long) {
        val current = getTodayStatsSync(date)
        dailyStatDao.upsert(
            current.copy(
                idleTimeSeconds = current.idleTimeSeconds + secondsToAdd
            )
        )
    }

    suspend fun recordFocusTime(date: String, secondsToAdd: Long) {
        val current = getTodayStatsSync(date)
        dailyStatDao.upsert(
            current.copy(
                focusTimeSeconds = current.focusTimeSeconds + secondsToAdd
            )
        )
    }

    suspend fun recordNudgeTriggered(date: String) {
        val current = getTodayStatsSync(date)
        dailyStatDao.upsert(
            current.copy(
                nudgesTriggered = current.nudgesTriggered + 1
            )
        )
    }

    suspend fun recordNudgeAction(
        date: String,
        action: String,
        intervalMinutes: Int,
        continuousActiveSeconds: Long
    ) {
        val current = getTodayStatsSync(date)
        val isSnooze = action.startsWith("SNOOZED")
        val isAck = action == "ACKNOWLEDGED" || action == "STARTED_FOCUS"

        dailyStatDao.upsert(
            current.copy(
                nudgesSnoozed = if (isSnooze) current.nudgesSnoozed + 1 else current.nudgesSnoozed,
                nudgesAcknowledged = if (isAck) current.nudgesAcknowledged + 1 else current.nudgesAcknowledged
            )
        )

        nudgeEventDao.insertEvent(
            NudgeEventEntity(
                triggerIntervalMinutes = intervalMinutes,
                continuousActiveSeconds = continuousActiveSeconds,
                actionTaken = action,
                date = date
            )
        )
    }

    suspend fun recordCompletedFocusSession(date: String, durationMinutes: Int, tag: String) {
        val current = getTodayStatsSync(date)
        dailyStatDao.upsert(
            current.copy(
                focusTimeSeconds = current.focusTimeSeconds + (durationMinutes * 60L),
                focusSessionsCompleted = current.focusSessionsCompleted + 1
            )
        )
        focusSessionDao.insertSession(
            FocusSessionEntity(
                durationMinutes = durationMinutes,
                completed = true,
                taskTag = tag,
                date = date
            )
        )
    }

    suspend fun seedSampleWeekData() {
        dailyStatDao.clearAll()
        nudgeEventDao.clearAll()
        focusSessionDao.clearAll()

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()

        val sampleDays = listOf(
            Triple(115 * 60L, 55 * 60L, 75 * 60L),
            Triple(140 * 60L, 70 * 60L, 60 * 60L),
            Triple(105 * 60L, 45 * 60L, 90 * 60L),
            Triple(125 * 60L, 60 * 60L, 80 * 60L),
            Triple(90 * 60L, 35 * 60L, 100 * 60L),
            Triple(80 * 60L, 30 * 60L, 120 * 60L),
            Triple(45 * 60L, 18 * 60L, 50 * 60L)
        )

        for (i in 0 until 7) {
            calendar.time = Date()
            calendar.add(Calendar.DAY_OF_YEAR, -(6 - i))
            val dateStr = sdf.format(calendar.time)
            val (screenSec, idleSec, focusSec) = sampleDays[i]

            val nudgesTrig = (screenSec / (15 * 60)).toInt().coerceAtLeast(3)
            val snoozed = (nudgesTrig * 0.4).toInt()
            val ack = nudgesTrig - snoozed

            dailyStatDao.upsert(
                DailyStatEntity(
                    date = dateStr,
                    screenTimeSeconds = screenSec,
                    idleTimeSeconds = idleSec,
                    focusTimeSeconds = focusSec,
                    nudgesTriggered = nudgesTrig,
                    nudgesSnoozed = snoozed,
                    nudgesAcknowledged = ack,
                    focusSessionsCompleted = (focusSec / (25 * 60)).toInt().coerceAtLeast(1)
                )
            )
        }
    }

    suspend fun clearAll() {
        dailyStatDao.clearAll()
        nudgeEventDao.clearAll()
        focusSessionDao.clearAll()
    }
}
