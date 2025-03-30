package com.txstate.taskbuddy.database
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
@Dao
interface UserDao {

    @Insert
     fun insert(user: Users): Long  // The insert method should return the row ID of the inserted user.

    @Query("SELECT * FROM user_table WHERE email = :email AND password = :password LIMIT 1")
     fun getUserByEmailAndPassword(email: String, password: String): Users?

    @Query("SELECT * FROM user_table WHERE email = :email LIMIT 1")
     fun getUserByEmail(email: String): Users?

     @Query("Delete FROM user_table")
     fun deleteAll()
//
//    // Ensure this query correctly selects the columns that match your entity
//    @Query("SELECT * FROM user_table WHERE email = :email AND password = :password LIMIT 1")
//     fun getUserByEmailAndPassword(email: String, password: String): User?
}
