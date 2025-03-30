package com.txstate.taskbuddy.database

import android.content.Context
import android.content.SharedPreferences
import kotlin.math.log

class AuthManager(private val context: Context, private val userRepository: UserRepository) {

    private val sharedPrefs: SharedPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    // Save login state
    public fun saveLoginState(isLoggedIn: Boolean) {
        val editor = sharedPrefs.edit()
        editor.putBoolean("is_logged_in", isLoggedIn)
        editor.apply()
    }

    // Check if user is logged in
    fun isLoggedIn(): Boolean {
        return sharedPrefs.getBoolean("is_logged_in", false)
    }

    // Login function
    suspend fun login(email: String, password: String): Boolean {
        val user = userRepository.getUserByEmailAndPassword(email, password)
        return if (user != null) {
            saveLoginState(true)
            saveUserDetails(user)
            true
        } else {
            false
        }
    }

    private fun saveUserDetails(user: Users) {
         val sharedPref = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
         with(sharedPref.edit()) {
             putInt("user_id", user.id)
             putString("user_email", user.email)
             putString("user_name", user.name)
             apply()
         }
     }
    // Register function
     fun register(email: String, password: String, name: String): Boolean {
        val existingUser = userRepository.getUserByEmail(email)
        if (existingUser == null) {
            userRepository.insert(Users(
                email = email, password = password,
                name = name
            ))
            return true
        }
        return false
    }
    fun getLoggedInUser(): Users? {
        val sharedPref = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val userId = sharedPref.getInt("user_id", -1)
        val userEmail = sharedPref.getString("user_email", null)
        val userName = sharedPref.getString("user_name", null)

        return if (userId != -1 && userEmail != null && userName != null) {
            Users(id = userId, email = userEmail, name = userName, password = "")
        } else {
            null
        }
    }
    // Logout function
    fun logout() {
        saveLoginState(false)
        clearUserDetails()
    }
    private fun clearUserDetails() {
        val sharedPref = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            remove("user_id")
            remove("user_email")
            remove("user_name")
            apply()
        }
    }
        
}
