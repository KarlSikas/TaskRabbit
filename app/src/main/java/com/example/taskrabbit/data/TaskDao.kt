package com.example.taskrabbit.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow // <<< Ensure Flow is imported
import org.threeten.bp.LocalDate // <<< Ensure LocalDate is imported

@Dao
interface TaskDao {
    // Keep existing queries...
    @Query("SELECT * FROM tasks WHERE dueDate = :date ORDER BY createdAt DESC")
    fun getTasksForDate(date: LocalDate): Flow<List<TaskItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskItem): Long

    @Update
    suspend fun updateTask(task: TaskItem)

    @Delete
    suspend fun deleteTask(task: TaskItem)

    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getTaskById(taskId: Long): TaskItem?

    @Query("SELECT DISTINCT dueDate FROM tasks WHERE dueDate IS NOT NULL ORDER BY dueDate ASC")
    fun getDatesWithTasks(): Flow<List<LocalDate>>

    @Query("SELECT * FROM tasks WHERE dueDate = :date AND isImportant = 1 ORDER BY createdAt DESC")
    fun getPriorityTasksForDate(date: LocalDate): Flow<List<TaskItem>>

    // --- Title query (keep as is) ---
    @Query("SELECT title FROM tasks WHERE dueDate = :date AND isImportant = 1 ORDER BY createdAt DESC")
    fun getPriorityTaskTitlesForDate(date: LocalDate): Flow<List<String>>

    // --- Count query (keep as is) ---
    /**
     * Returns a Flow emitting the count of priority tasks for a specific date.
     */
    @Query("SELECT COUNT(*) FROM tasks WHERE dueDate = :date AND isImportant = 1")
    fun getPriorityTaskCountOnDate(date: LocalDate): Flow<Int>

    // --- ADDED FUNCTION FOR BOOT RECEIVER (Option B) ---
    /**
     * Fetches tasks that have non-null reminder details, suitable for rescheduling.
     * This is a suspend function intended for background execution (like in BootReceiver).
     */
    @Query("SELECT * FROM tasks WHERE reminderMinutesBefore IS NOT NULL AND taskTime IS NOT NULL AND dueDate IS NOT NULL")
    suspend fun getTasksWithPotentialReminders(): List<TaskItem> // <<< ADDED
    // --- END ADDED FUNCTION ---
}