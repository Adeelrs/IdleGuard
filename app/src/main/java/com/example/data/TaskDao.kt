package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM weekly_tasks WHERE dayOfWeek = :dayOfWeek ORDER BY isCompleted ASC, createdAt ASC")
    fun getTasksForDay(dayOfWeek: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM weekly_tasks ORDER BY createdAt ASC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Query("SELECT COUNT(*) FROM weekly_tasks")
    suspend fun getTaskCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<TaskEntity>)

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    @Query("DELETE FROM weekly_tasks WHERE id = :taskId")
    suspend fun deleteTaskById(taskId: Long)

    @Query("UPDATE weekly_tasks SET isCompleted = :isCompleted WHERE id = :taskId")
    suspend fun updateCompletion(taskId: Long, isCompleted: Boolean)

    @Query("UPDATE weekly_tasks SET title = :title WHERE id = :taskId")
    suspend fun updateTitle(taskId: Long, title: String)
}
