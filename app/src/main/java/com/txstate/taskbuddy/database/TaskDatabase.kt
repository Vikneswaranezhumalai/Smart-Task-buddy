package com.txstate.taskbuddy.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Task::class,Users::class], version = 4)
abstract class TaskDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun userDao(): UserDao


    companion object {
        // Singleton instance of the database
        @Volatile
        private var INSTANCE: TaskDatabase? = null

        // Method to get the singleton instance of the database
        fun getDatabase(context: Context): TaskDatabase {
            // If the database is already created, return it
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TaskDatabase::class.java,
                    "task_database"
                ).fallbackToDestructiveMigration().
                build()
                INSTANCE = instance
                instance
            }
        }
    }
}
