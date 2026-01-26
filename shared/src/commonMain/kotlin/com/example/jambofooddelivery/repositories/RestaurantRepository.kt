package com.example.jambofooddelivery.repositories

import com.example.jambofooddelivery.cache.Database
import com.example.jambofooddelivery.models.Address
import com.example.jambofooddelivery.models.Location
import com.example.jambofooddelivery.models.Restaurant
import com.example.jambofooddelivery.models.MenuItem
import com.example.jambofooddelivery.models.MenuCategory
import com.example.jambofooddelivery.remote.ApiService
import com.example.jambofooddelivery.utils.Result
import io.github.aakira.napier.Napier

interface RestaurantRepository {
    suspend fun getRestaurants(location: Location): Result<List<Restaurant>>
    suspend fun getRestaurantById(id: String): Result<Restaurant>
    suspend fun searchRestaurants(query: String, location: Location): Result<List<Restaurant>>
    suspend fun getRestaurantMenu(restaurantId: String): Result<Restaurant>
    suspend fun getNearbyRestaurants(location: Location, radius: Int = 5000): Result<List<Restaurant>>
    suspend fun getCategoryItems(categoryId: String): Result<List<MenuItem>>
    suspend fun getRestaurantCategories(restaurantId: String): Result<List<MenuCategory>>
}

class RestaurantRepositoryImpl(
    private val apiService: ApiService,
    private val db: Database,
) : RestaurantRepository {

    override suspend fun getRestaurants(location: Location): Result<List<Restaurant>> {
        return try {
            val response = apiService.getRestaurants(location)
            if (response.success && response.data != null) {
                // Save to local DB
                response.data.forEach { restaurant ->
                    try {
                        db.restaurantQueries.insertRestaurant(
                            id = restaurant.id,
                            name = restaurant.name,
                            description = restaurant.description,
                            cover_image_url = restaurant.coverImageUrl,
                            logo_url = restaurant.logoUrl,
                            address = restaurant.address.fullAddress,
                            latitude = restaurant.location.latitude,
                            longitude = restaurant.location.longitude,
                            rating = restaurant.ratingDouble,
                            delivery_time_range = restaurant.deliveryTimeRange,
                            minimum_order = restaurant.minimumOrderDouble,
                            delivery_fee = restaurant.deliveryFeeDouble,
                            is_active = restaurant.isActive
                        )
                    } catch (e: Exception) {
                        Napier.e("Failed to cache restaurant ${restaurant.id}: ${e.message}")
                    }
                }
                Result.Success(response.data)
            } else {
                loadFromCache("API Error: ${response.error}")
            }
        } catch (e: Exception) {
             Napier.e("Network fetch failed: ${e.message}")
             loadFromCache("Network error: ${e.message}")
        }
    }

    private fun loadFromCache(errorMessage: String): Result<List<Restaurant>> {
        return try {
            val cached = db.restaurantQueries.getRestaurants().executeAsList()
            if (cached.isNotEmpty()) {
                Result.Success(cached.map { it.toDomain() })
            } else {
                Result.Error(errorMessage)
            }
        } catch (e: Exception) {
            Result.Error("Cache error: ${e.message}")
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
        return try {
            val response = apiService.searchRestaurants(query, location)
            if (response.success && response.data != null) {
                Result.Success(response.data)
            } else {
                Result.Error(response.error ?: "Search failed")
            }
        } catch (e: Exception) {
            Result.Error("Search error: ${e.message}")
        }
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
        return try {
            val response = apiService.getNearbyRestaurants(location, radius)
            if (response.success && response.data != null) {
                Result.Success(response.data)
            } else {
                Result.Error(response.error ?: "Failed to load nearby restaurants")
            }
        } catch (e: Exception) {
            Result.Error("Nearby fetch failed: ${e.message}")
        }
    }

    override suspend fun getRestaurantCategories(restaurantId: String): Result<List<MenuCategory>> {
        return try {
            val response = apiService.getRestaurantCategories(restaurantId)
            if (response.success && response.data != null) {
                Result.Success(response.data)
            } else {
                Result.Error(response.error ?: "Failed to load categories")
            }
        } catch (e: Exception) {
            Result.Error("Categories fetch failed: ${e.message}")
        }
    }

    override suspend fun getCategoryItems(categoryId: String): Result<List<MenuItem>> {
        return try {
            val response = apiService.getCategoryItems(categoryId)
            if (response.success && response.data != null) {
                Result.Success(response.data)
            } else {
                Result.Error(response.error ?: "Failed to load items")
            }
        } catch (e: Exception) {
            Result.Error("Items fetch failed: ${e.message}")
        }
    }

    private fun com.example.jambofooddelivery.cache.Restaurant.toDomain(): Restaurant {
        return Restaurant(
            id = id,
            name = name,
            description = description,
            coverImageUrl = cover_image_url,
            logoUrl = logo_url,
            address = Address(
                street = address,
                city = "",
                state = "",
                postalCode = "",
                country = "",
                latitude = latitude,
                longitude = longitude
            ),
            rating = rating.toString(),
            deliveryTimeRange = delivery_time_range,
            minimumOrder = minimum_order.toString(),
            deliveryFee = delivery_fee.toString(),
            isActive = is_active ?: true,
            latitude = latitude,
            longitude = longitude,
            categories = emptyList()
        )
    }
}
