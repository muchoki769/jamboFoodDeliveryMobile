package com.jambofooddelivery.repositories

import com.jambofooddelivery.models.User
import com.jambofooddelivery.preferences.AppSettings
import com.jambofooddelivery.remote.ApiService
import com.jambofooddelivery.remote.LoginRequest
import com.jambofooddelivery.remote.RegisterRequest
import com.jambofooddelivery.utils.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class AuthRepositoryImpl(
    private val apiService: ApiService,
    private val appSettings: AppSettings,
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<User> {
        return try {
            val response = apiService.login(LoginRequest(email, password))
            if (response.success && response.data != null) {
                val authData = response.data
                appSettings.saveUser(authData.user)
                
                val token = authData.token ?: authData.accessToken
                if (token != null) appSettings.saveToken(token)

                Result.Success(authData.user)
            } else {
                Result.Error(response.error ?: "Login failed")
            }
        } catch (e: Exception) {
            Result.Error("Network error: ${e.message}")
        }
    }

    override suspend fun register(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        phone: String
    ): Result<User> {
        return try {
            val response = apiService.register(
                RegisterRequest(email, password, firstName, lastName, phone)
            )
            if (response.success && response.data != null) {
                val authData = response.data
                appSettings.saveUser(authData.user)
                
                val token = authData.token ?: authData.accessToken
                if (token != null) appSettings.saveToken(token)
                
                Result.Success(authData.user)
            } else {
                Result.Error(response.error ?: "Registration failed")
            }
        } catch (e: Exception) {
            Result.Error("Registration failed: ${e.message}")
        }
    }

    override suspend fun logout() {
        appSettings.clearUser()
        appSettings.clearToken()
    }

    override fun getCurrentUser(): Flow<User?> = flow {
        emit(appSettings.getCurrentUser())
    }

    override suspend fun isLoggedIn(): Boolean {
        return appSettings.getToken() != null
    }
}
