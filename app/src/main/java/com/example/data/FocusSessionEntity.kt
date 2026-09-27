package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "focus_sessions")
data class FocusSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val timestamp: Long = System.currentTimeMillis(),
    val durationMinutes: Int,
    val completed: Boolean,
    val taskTag: String = "Deep Work",
    val date: String // YYYY-MM-DD
)
