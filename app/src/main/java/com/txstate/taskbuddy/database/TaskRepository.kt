package com.txstate.taskbuddy.database

import androidx.lifecycle.LiveData

class TaskRepository(private val taskDao: TaskDao) {

    // Insert a task
     fun insertTask(task: Task) {
        taskDao.insertTask(task)
    }

    // Get all tasks
    fun getAllTasks(userId: Int): LiveData<List<Task>> {
        return taskDao.getAllTasks(userId)
    }
    // Get frequent categories
    fun getFrequentCategories(sinceDate: String): List<CategorySummary> {
        return taskDao.getFrequentCategories(sinceDate)
    }

    // Get Completed tasks
    fun getCompletedTask(userId: Int): LiveData<List<Task>> {
        return taskDao.getCompletedTasks(userId)
    }
    fun getCompletedTasks(userId: Int): List<Task> {
        return taskDao.getCompletedTask(userId)
    }
    fun getCompletedTaskCount(userId: Int): Int {
        return taskDao.getCompletedTaskCount(userId)
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
