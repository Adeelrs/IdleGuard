package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyStatDao {
    @Query("SELECT * FROM daily_stats WHERE date = :date LIMIT 1")
    fun getDailyStat(date: String): Flow<DailyStatEntity?>

    @Query("SELECT * FROM daily_stats WHERE date = :date LIMIT 1")
    suspend fun getDailyStatSync(date: String): DailyStatEntity?

    @Query("SELECT * FROM daily_stats ORDER BY date DESC LIMIT :limit")
    fun getRecentStats(limit: Int = 7): Flow<List<DailyStatEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(stat: DailyStatEntity)

    @Query("DELETE FROM daily_stats")
    suspend fun clearAll()
}
