package com.example.jambofooddelivery.repositories

import com.example.jambofooddelivery.models.Notification
import com.example.jambofooddelivery.remote.ApiService
import com.example.jambofooddelivery.utils.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

interface NotificationRepository {
    suspend fun getNotifications(userId: String): Result<List<Notification>>
    suspend fun markAsRead(notificationId: String): Result<Unit>
    suspend fun registerToken(userId: String, token: String): Result<Unit>
}

class NotificationRepositoryImpl(
    private val apiService: ApiService
) : NotificationRepository {
    override suspend fun getNotifications(userId: String): Result<List<Notification>> {
        return try {
            val response = apiService.getNotifications(userId)
            if (response.success && response.data != null) {
                Result.Success(response.data)
            } else {
                Result.Error(response.error ?: "Failed to fetch notifications")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Unknown error")
        }
    }

    override suspend fun markAsRead(notificationId: String): Result<Unit> {
        return try {
            val response = apiService.markNotificationAsRead(notificationId)
            if (response.success) {
                Result.Success(Unit)
            } else {
                Result.Error(response.error ?: "Failed to mark as read")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Unknown error")
        }
    }

    override suspend fun registerToken(userId: String, token: String): Result<Unit> {
        return try {
            val response = apiService.registerFcmToken(userId, token)
            if (response.success) {
                Result.Success(Unit)
            } else {
                Result.Error(response.error ?: "Failed to register token")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Unknown error")
        }
    }
}
