package com.jambofooddelivery.domain

import com.jambofooddelivery.models.Location
import com.jambofooddelivery.models.Restaurant
import com.jambofooddelivery.repositories.RestaurantRepository
import com.jambofooddelivery.utils.Result

class GetRestaurantsUseCase(
    private val restaurantRepository: RestaurantRepository
) {
    suspend operator fun invoke(location: Location): Result<List<Restaurant>> {
        return restaurantRepository.getRestaurants(location)
    }
}

class SearchRestaurantsUseCase(
    private val restaurantRepository: RestaurantRepository
) {
    suspend operator fun invoke(query: String, location: Location): Result<List<Restaurant>> {
        return restaurantRepository.searchRestaurants(query, location)
    }
}
