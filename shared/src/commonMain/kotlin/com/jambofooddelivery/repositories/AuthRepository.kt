package com.jambofooddelivery.repositories

import com.jambofooddelivery.models.User
import com.jambofooddelivery.utils.Result
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<User>
    suspend fun register(email: String, password: String, firstName: String, lastName: String, phone: String): Result<User>
    suspend fun logout()
    fun getCurrentUser(): Flow<User?>
    suspend fun isLoggedIn(): Boolean
}