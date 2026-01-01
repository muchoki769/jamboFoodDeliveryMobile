package com.example.jambofooddelivery.domain

import com.example.jambofooddelivery.models.Location
import com.example.jambofooddelivery.models.Restaurant
import com.example.jambofooddelivery.repositories.RestaurantRepository
import com.example.jambofooddelivery.utils.Result

class GetRestaurantsUseCase(
    private val restaurantRepository: RestaurantRepository
) {
    suspend operator fun invoke(location: Location): Result<List<Restaurant>> {
        return restaurantRepository.getRestaurants(location)
    }
}