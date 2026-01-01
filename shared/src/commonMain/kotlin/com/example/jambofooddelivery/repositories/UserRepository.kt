package com.example.jambofooddelivery.repositories

import com.example.jambofooddelivery.models.ProfileUpdate
import com.example.jambofooddelivery.models.User
import com.example.jambofooddelivery.preferences.AppSettings
import com.example.jambofooddelivery.remote.ApiService
import com.example.jambofooddelivery.remote.CloudinaryService
import com.example.jambofooddelivery.utils.Result
import com.example.jambofooddelivery.remote.ProfileUpdate as RemoteProfileUpdate

interface UserRepository {
    suspend fun getCurrentUser(): User?
    suspend fun updateUser(user: User): Result<User>
    suspend fun updateProfile(updates: ProfileUpdate): Result<User>
    suspend fun uploadProfileImage(imageBytes: ByteArray): Result<String>
}

class UserRepositoryImpl(
    private val apiService: ApiService,
    private val appSettings: AppSettings,
    private val cloudinaryService: CloudinaryService
) : UserRepository {

    override suspend fun getCurrentUser(): User? {
        return appSettings.getCurrentUser()
    }


    override suspend fun updateUser(user: User): Result<User> {
        return try {
            val response = apiService.updateUser(user.id, user)
            if (response.success && response.data != null) {
                appSettings.saveUser(response.data)
                Result.Success(response.data)
            } else {
                Result.Error(response.error ?: "Failed to update user")
            }
        } catch (e: Exception) {
            Result.Error("Update failed: ${e.message}")
        }
    }

    override suspend fun updateProfile(updates: ProfileUpdate): Result<User> {
        return try {
            // Map from models.ProfileUpdate to remote.ProfileUpdate
            val remoteUpdates = RemoteProfileUpdate(
                firstName = updates.firstName,
                lastName = updates.lastName,
                phone = updates.phone,
                avatarUrl = updates.avatarUrl
            )
            val response = apiService.updateProfile(remoteUpdates)
            if (response.success && response.data != null) {
                appSettings.saveUser(response.data)
                Result.Success(response.data)
            } else {
                Result.Error(response.error ?: "Failed to update profile")
            }
        } catch (e: Exception) {
            Result.Error("Profile update failed: ${e.message}")
        }
    }

    override suspend fun uploadProfileImage(imageBytes: ByteArray): Result<String> {
        return try {
            val imageUrl = cloudinaryService.uploadImage(imageBytes)
            if (imageUrl != null) {
                // Update user profile with new image URL
                val currentUser = appSettings.getCurrentUser()
                currentUser?.let { user ->
                    val updatedUser = user.copy(avatarUrl = imageUrl)
                    appSettings.saveUser(updatedUser)
                }
                Result.Success(imageUrl)
            } else {
                Result.Error("Failed to upload image")
            }
        } catch (e: Exception) {
            Result.Error("Image upload failed: ${e.message}")
        }
    }
}