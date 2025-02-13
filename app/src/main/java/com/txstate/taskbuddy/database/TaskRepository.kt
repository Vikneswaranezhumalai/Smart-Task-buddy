package com.txstate.taskbuddy.database

import androidx.lifecycle.LiveData

class TaskRepository(private val taskDao: TaskDao) {

    // Insert a task
     fun insertTask(task: Task) {
        taskDao.insertTask(task)
    }

    // Get all tasks
    fun getAllTasks(): LiveData<List<Task>> {
        return taskDao.getAllTasks()
    }

    // Get Completed tasks
    fun getCompletedTask(): LiveData<List<Task>> {
        return taskDao.getCompletedTasks()
    }

    // Update a task
    fun updateTask(task: Task) {
        taskDao.updateTask(task)
    }

    // Delete a task
    fun deleteTask(task: Task) {
        taskDao.deleteTask(task)
    }

    // Get task by ID
    fun getTaskById(taskId: Int): LiveData<Task> {
        return taskDao.getTaskById(taskId)
    }
}
