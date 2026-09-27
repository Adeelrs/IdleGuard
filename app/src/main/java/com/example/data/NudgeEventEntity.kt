package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "nudge_events")
data class NudgeEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val timestamp: Long = System.currentTimeMillis(),
    val triggerIntervalMinutes: Int,
    val continuousActiveSeconds: Long,
    val actionTaken: String, // "SNOOZED_5M", "SNOOZED_10M", "SNOOZED_15M", "SNOOZED_CUSTOM", "ACKNOWLEDGED", "STARTED_FOCUS"
    val date: String // YYYY-MM-DD
)
