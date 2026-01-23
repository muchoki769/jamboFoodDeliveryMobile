package com.example.jambofooddelivery.ui.ViewModels


import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Fastfood
import androidx.compose.material.icons.outlined.Icecream
import androidx.compose.material.icons.outlined.LocalDrink
import androidx.compose.material.icons.outlined.LocalPizza
import com.example.jambofooddelivery.domain.GetRestaurantsUseCase
import com.example.jambofooddelivery.domain.UpdateLocationUseCase
import com.example.jambofooddelivery.models.Location
import com.example.jambofooddelivery.models.Restaurant
import com.example.jambofooddelivery.repositories.LocationRepository
import com.example.jambofooddelivery.utils.Result
import com.example.jambofooddelivery.data.Category
import com.example.jambofooddelivery.preferences.AppSettings
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject



data class HomeState(
    val isLoading: Boolean = false,
    val featuredRestaurants: List<Restaurant> = emptyList(),
    val nearbyRestaurants: List<Restaurant> = emptyList(),
    val categories: List<Category> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: String? = null,
    val error: String? = null,
    val userLocation: Location? = null,
    val address: String? = null
)

sealed class HomeEvent {
    data class NavigateToRestaurant(val restaurantId: String) : HomeEvent()
    data class ShowError(val message: String) : HomeEvent()
    object LocationPermissionDenied : HomeEvent()
}

class HomeViewModel : BaseViewModel<HomeState, HomeEvent>(HomeState()), KoinComponent {

    private val getRestaurantsUseCase: GetRestaurantsUseCase by inject()
    private val locationRepository: LocationRepository by inject()
    private val updateLocationUseCase: UpdateLocationUseCase by inject()
    private val appSettings: AppSettings by inject()

    init {
        loadCategories()
        requestLocationAndLoadRestaurants()
    }

    fun loadRestaurants() {
        launch {
            val location = state.value.userLocation
            if (location == null) {
                Log.d("HomeViewModel", "Cannot load restaurants: userLocation is null")
                setState { it.copy(address = "Update Location") }
                return@launch
            }
            
            Log.d("HomeViewModel", "Loading restaurants for: ${location.latitude}, ${location.longitude}")
            setState { it.copy(isLoading = true, error = null) }
            
            when (val result = getRestaurantsUseCase(location)) {
                is Result.Success -> {
                    Log.d("HomeViewModel", "Loaded ${result.data.size} restaurants from repository")
                    setState {
                        it.copy(
                            isLoading = false,
                            featuredRestaurants = result.data.filter { r -> r.ratingDouble >= 4.0 },
                            nearbyRestaurants = result.data
                        )
                    }
                }

                is Result.Error -> {
                    Log.e("HomeViewModel", "Error loading restaurants: ${result.message}")
                    setState {
                        it.copy(
                            isLoading = false,
                            error = result.message
                        )
                    }
                    emitEvent(HomeEvent.ShowError(result.message))
                }
                is Result.Loading -> {
                }
            }
        }
    }

    fun searchRestaurants(query: String) {
        setState { it.copy(searchQuery = query) }

        if (query.length >= 3) {
            launch {
                val location = state.value.userLocation
                if (location != null) {
                    // Implement search logic here
                }
            }
        }
    }

    fun filterByCategory(category: Category) {
        setState { it.copy(selectedCategory = category.name) }
    }

    fun onRestaurantClick(restaurantId: String) {
        emitEvent(HomeEvent.NavigateToRestaurant(restaurantId))
    }

    fun retry() {
        requestLocationAndLoadRestaurants()
    }

    fun clearError() {
        setState { it.copy(error = null) }
    }


    private fun loadCategories() {
        val categories = listOf(
            Category("Pizza", Icons.Outlined.LocalPizza),
            Category("Burgers", Icons.Outlined.Fastfood),
            Category("Drinks", Icons.Outlined.LocalDrink),
            Category("Desserts", Icons.Outlined.Icecream)
        )


        setState { it.copy(categories = categories) }
    }



    private fun requestLocationAndLoadRestaurants() {
        launch {
            // 1. Check permissions and get fresh location
            if (locationRepository.hasLocationPermission()) {
                val location = locationRepository.getCurrentLocation()
                
                if (location != null) {
                    val addressResult = locationRepository.reverseGeocode(location)
                    val addressStr = if (addressResult is Result.Success) addressResult.data else null
                    
                    appSettings.saveCachedLocation(location, addressStr)
                    setState { it.copy(userLocation = location, address = addressStr) }
                    
                    updateLocationUseCase(location, "")
                    loadRestaurants()
                    return@launch
                }
            }

            // 2. Fallback to cache if fresh failed
            val cachedLocation = appSettings.getCachedLocation()
            val cachedAddress = appSettings.getCachedAddress()
            
            if (cachedLocation != null) {
                Log.d("HomeViewModel", "Using cached location: ${cachedLocation.latitude}")
                setState { it.copy(userLocation = cachedLocation, address = cachedAddress) }
                loadRestaurants()
            } else {
                Log.d("HomeViewModel", "No location available (fresh or cached)")
                setState { it.copy(address = "Update Location") }
                emitEvent(HomeEvent.LocationPermissionDenied)
            }
        }
    }
}
