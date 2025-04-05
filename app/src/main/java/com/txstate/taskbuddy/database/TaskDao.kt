package com.txstate.taskbuddy.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface TaskDao {
    @Insert
     fun insertTask(task: Task)

    @Query("SELECT * FROM task_table WHERE priority != 'Completed' AND userId = :userId")
    fun getAllTasks(userId: Int): LiveData<List<Task>>

    @Query("SELECT * FROM task_table WHERE priority == 'Completed' AND userId = :userId")
    fun getCompletedTasks(userId: Int): LiveData<List<Task>>

    @Query("SELECT * FROM task_table WHERE priority == 'Completed' AND userId = :userId")
    fun getCompletedTask(userId: Int): List<Task>

    @Query("SELECT COUNT(*) FROM task_table WHERE userId = :userId AND priority == 'Completed'")
     fun getCompletedTaskCount(userId: Int): Int

    @Query("""
        SELECT category, COUNT(*) as count
        FROM task_table
        WHERE dueDate >= :startDate
        GROUP BY category
        ORDER BY count DESC
        LIMIT 5
    """)
    fun getFrequentCategories(startDate: String): List<CategorySummary>

    @Update
     fun updateTask(task: Task)

    @Delete
     fun deleteTask(task: Task)

    // Get Task by ID
    @Query("SELECT * FROM task_table WHERE id = :taskId")
     fun getTaskById(taskId: Int): LiveData<Task>
}
