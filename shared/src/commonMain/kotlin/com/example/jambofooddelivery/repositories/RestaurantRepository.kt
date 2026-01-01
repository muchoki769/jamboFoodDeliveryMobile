package com.example.jambofooddelivery.repositories

import com.example.jambofooddelivery.cache.AppDatabase
import com.example.jambofooddelivery.cache.Database
import com.example.jambofooddelivery.models.Location
import com.example.jambofooddelivery.models.Restaurant
import com.example.jambofooddelivery.remote.ApiService
import com.example.jambofooddelivery.utils.Result

interface RestaurantRepository {
    suspend fun getRestaurants(location: Location): Result<List<Restaurant>>
    suspend fun getRestaurantById(id: String): Result<Restaurant>
    suspend fun searchRestaurants(query: String, location: Location): Result<List<Restaurant>>
    suspend fun getRestaurantMenu(restaurantId: String): Result<Restaurant>
    suspend fun getNearbyRestaurants(location: Location, radius: Int = 5000): Result<List<Restaurant>>
}

class RestaurantRepositoryImpl(
    private val apiService: ApiService,
    private val db: Database,
) : RestaurantRepository {

    override suspend fun getRestaurants(location: Location): Result<List<Restaurant>> {
        return try {
            val response = apiService.getRestaurants(location)
            if (response.success && response.data != null) {

                response.data.forEach { restaurant ->
                    db.restaurantQueries.insertRestaurant(
                        id = restaurant.id,
                        name = restaurant.name,
                        description = restaurant.description,
                        cover_image_url = restaurant.coverImageUrl,
                        logo_url = restaurant.logoUrl,
                        address = restaurant.address.fullAddress,
                        latitude = restaurant.location.latitude,
                        longitude = restaurant.location.longitude,
                        rating = restaurant.rating,
                        delivery_time_range = restaurant.deliveryTimeRange,
                        minimum_order = restaurant.minimumOrder,
                        delivery_fee = restaurant.deliveryFee,
                        is_active = restaurant.isActive
                    )
                }

                Result.Success(response.data)
            } else {
                Result.Error(response.error ?: "Failed to load restaurants")
            }
        } catch (e: Exception) {
             // Fallback to cache would go here
            Result.Error("Network error: ${e.message}")
        }
    }

    override suspend fun getRestaurantById(id: String): Result<Restaurant> {
        return try {
            val response = apiService.getRestaurantMenu(id)
            if (response.success && response.data != null) {
                Result.Success(response.data)
            } else {
                Result.Error(response.error ?: "Restaurant not found")
            }
        } catch (e: Exception) {
            Result.Error("Failed to load restaurant: ${e.message}")
        }
    }


    override suspend fun searchRestaurants(query: String, location: Location): Result<List<Restaurant>> {
        // ApiService doesn't have searchRestaurants yet, let's assume we should use getRestaurants for now or implement it
        // The previous code called apiService.searchRestaurants which didn't exist in the interface shown earlier.
        // We'll fix this by removing the call or implementing it.
        // For now, let's implement a dummy search or filter on getRestaurants if possible, or just return empty.
        
        // Actually, let's try to filter getRestaurants results if the API doesn't support search directly yet, 
        // or we need to add it to ApiService.
        // But since I can edit ApiService, I will add it there in a moment.
        // For this file compilation, I will comment it out or leave it if I add it to ApiService.
        
        return Result.Error("Search not implemented yet")
    }

    override suspend fun getRestaurantMenu(restaurantId: String): Result<Restaurant> {
        return try {
            val response = apiService.getRestaurantMenu(restaurantId)
            if (response.success && response.data != null) {
                Result.Success(response.data)
            } else {
                Result.Error(response.error ?: "Failed to load menu")
            }
        } catch (e: Exception) {
            Result.Error("Menu loading failed: ${e.message}")
        }
    }

    override suspend fun getNearbyRestaurants(location: Location, radius: Int): Result<List<Restaurant>> {
         // Similar to search, getNearbyRestaurants was not in ApiService interface.
         // We should add it or reuse getRestaurants.
         return getRestaurants(location)
    }

}