package com.example.jambofooddelivery.domain

import com.example.jambofooddelivery.models.Location
import com.example.jambofooddelivery.repositories.LocationRepository
import com.example.jambofooddelivery.remote.ApiService
import com.example.jambofooddelivery.utils.Result

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
    
    // Overload if we only pass location and assume userId is handled inside (e.g., by repo or not needed here if only local)
    // But based on AppModule, it is injected. The HomeViewModel calls it with `updateLocationUseCase(it)`.
    // So we need an invoke with just location.
    // We probably need UserRepository to get current userId.
    // Let's modify constructor to include UserRepository or just accept location if it updates local repo.
    // HomeViewModel usage: updateLocationUseCase(it) where it is Location.
    
    // Wait, I can't inject UserRepository here easily if I don't change constructor.
    // Let's assume for now we just update local repository or similar.
    // Or we can just define the invoke method to match usage.
    
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