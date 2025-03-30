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

    @Update
     fun updateTask(task: Task)

    @Delete
     fun deleteTask(task: Task)

    // Get Task by ID
    @Query("SELECT * FROM task_table WHERE id = :taskId")
     fun getTaskById(taskId: Int): LiveData<Task>
}
