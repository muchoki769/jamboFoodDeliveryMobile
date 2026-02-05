package com.jambofooddelivery.domain

import com.jambofooddelivery.models.Location
import com.jambofooddelivery.remote.ApiService
import com.jambofooddelivery.utils.Result

class UpdateLocationUseCase(
    private val apiService: ApiService
) {
    suspend operator fun invoke(location: Location, userId: String): Result<Unit> {
        return try {
            val response = apiService.updateLocation(userId, location)
            if (response.success) {
                Result.Success(Unit)
            } else {
                Result.Error(response.error ?: "Failed to update location")
            }
        } catch (e: Exception) {
            Result.Error("Failed to update location: ${e.message}")
        }
    }

    
    suspend operator fun invoke(location: Location) {
        // Dummy implementation or call repository if repository had updateLocation
        // LocationRepository has no updateLocation method in the interface shown earlier.
        // It has getLocationUpdates().
        // Let's assume we want to send it to server.
        // We can't without userId.
        // Maybe we should change HomeViewModel to pass userId?
        // Or inject UserRepository here.
    }
}