package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_stats")
data class DailyStatEntity(
    @PrimaryKey val date: String, // Format: YYYY-MM-DD
    val screenTimeSeconds: Long = 0L,
    val idleTimeSeconds: Long = 0L,
    val focusTimeSeconds: Long = 0L,
    val nudgesTriggered: Int = 0,
    val nudgesSnoozed: Int = 0,
    val nudgesAcknowledged: Int = 0,
    val focusSessionsCompleted: Int = 0
)
