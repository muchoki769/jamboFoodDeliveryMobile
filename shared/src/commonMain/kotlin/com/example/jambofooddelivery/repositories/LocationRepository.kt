package com.example.jambofooddelivery.repositories

import com.example.jambofooddelivery.models.Location
import com.example.jambofooddelivery.remote.ApiService
import com.example.jambofooddelivery.utils.Result
import kotlinx.coroutines.flow.Flow

interface PlatformLocationService {
    suspend fun getCurrentLocation(): Location?
    fun getLocationUpdates(): Flow<Location>
    suspend fun hasLocationPermission(): Boolean
    suspend fun requestLocationPermission(): Boolean
}

interface LocationRepository {
    suspend fun getCurrentLocation(): Location?
    fun getLocationUpdates(): Flow<Location>
    suspend fun hasLocationPermission(): Boolean
    suspend fun requestLocationPermission(): Boolean
    suspend fun geocodeAddress(address: String): Result<Location>
    suspend fun reverseGeocode(location: Location): Result<String>
}

class LocationRepositoryImpl(
    private val platformLocationService: PlatformLocationService,
    private val apiService: ApiService
) : LocationRepository {
    override suspend fun getCurrentLocation(): Location? {
        return platformLocationService.getCurrentLocation()
    }

    override fun getLocationUpdates(): Flow<Location> {
        return platformLocationService.getLocationUpdates()
    }

    override suspend fun hasLocationPermission(): Boolean {
        return platformLocationService.hasLocationPermission()
    }

    override suspend fun requestLocationPermission(): Boolean {
        return platformLocationService.requestLocationPermission()
    }

    override suspend fun geocodeAddress(address: String): Result<Location> {
        return try {
            val response = apiService.geocodeAddress(address)
            if (response.success && response.data != null) {
                Result.Success(response.data)
            } else {
                Result.Error(response.error ?: "Address not found")
            }
        } catch (e: Exception) {
            Result.Error("Geocoding failed: ${e.message}")
        }
    }

    override suspend fun reverseGeocode(location: Location): Result<String> {
        return try {
            val response = apiService.reverseGeocode(location)
            if (response.success && response.data != null) {
                Result.Success(response.data)
            } else {
                Result.Error(response.error ?: "Reverse geocoding failed")
            }
        } catch (e: Exception) {
            Result.Error("Reverse geocoding failed: ${e.message}")
        }
    }
}



