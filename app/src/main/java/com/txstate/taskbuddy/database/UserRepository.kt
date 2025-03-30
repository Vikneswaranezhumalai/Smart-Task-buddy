package com.txstate.taskbuddy.database

class UserRepository(private val userDao: UserDao) {

     fun insert(user: Users) {
        userDao.insert(user)
    }

      fun getUserByEmailAndPassword(email: String, password: String): Users? {
        return userDao.getUserByEmailAndPassword(email, password)
    }
//
     fun getUserByEmail(email: String): Users? {
        return userDao.getUserByEmail(email)
    }
    fun deleteAll() {
        userDao.deleteAll()
    }
}
