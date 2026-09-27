package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NudgeEventDao {
    @Query("SELECT * FROM nudge_events ORDER BY timestamp DESC")
    fun getAllNudgeEvents(): Flow<List<NudgeEventEntity>>

    @Query("SELECT * FROM nudge_events WHERE date = :date ORDER BY timestamp DESC")
    fun getNudgeEventsForDate(date: String): Flow<List<NudgeEventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: NudgeEventEntity): Long

    @Query("SELECT COUNT(*) FROM nudge_events WHERE date = :date")
    suspend fun getCountForDate(date: String): Int

    @Query("DELETE FROM nudge_events")
    suspend fun clearAll()
}
