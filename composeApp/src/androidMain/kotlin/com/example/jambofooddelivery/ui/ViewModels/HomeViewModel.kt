package com.example.jambofooddelivery.ui.ViewModels


import androidx.compose.foundation.Image
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Fastfood
import androidx.compose.material.icons.outlined.Icecream
import androidx.compose.material.icons.outlined.LocalDrink
import androidx.compose.material.icons.outlined.LocalPizza
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.example.jambofooddelivery.domain.GetRestaurantsUseCase
import com.example.jambofooddelivery.domain.UpdateLocationUseCase
import com.example.jambofooddelivery.models.Location
import com.example.jambofooddelivery.models.Restaurant
import com.example.jambofooddelivery.repositories.LocationRepository
import com.example.jambofooddelivery.utils.Result
import com.example.jambofooddelivery.data.Category
import kotlinx.coroutines.flow.update
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt



data class HomeState(
    val isLoading: Boolean = false,
    val featuredRestaurants: List<Restaurant> = emptyList(),
    val nearbyRestaurants: List<Restaurant> = emptyList(),
    val categories: List<Category> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: String? = null,
    val error: String? = null,
    val userLocation: Location? = null
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

    init {
        loadCategories()
        requestLocationAndLoadRestaurants()
    }

    fun loadRestaurants() {
        launch {
            setState { it.copy(isLoading = true, error = null) }

            val location = state.value.userLocation
            if (location == null) {
                setState { it.copy(isLoading = false) }
                return@launch
            }
            when (val result = getRestaurantsUseCase(location)) {
                is Result.Success -> {
                    setState {
                        it.copy(
                            isLoading = false,
                            featuredRestaurants = result.data.filter { it.rating >= 4.5 },
                            nearbyRestaurants = result.data.sortedBy { restaurant ->
                                calculateDistance(
                                    state.value.userLocation!!,
                                    restaurant.location
                                )
                            }
                        )
                    }
                }

                is Result.Error -> {
                    setState {
                        it.copy(
                            isLoading = false,
                            error = result.message
                        )
                    }
                    emitEvent(HomeEvent.ShowError(result.message))
                }
                is Result.Loading -> {
                    // Already handled by initial setState
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
                    // This would call a search use case
                }
            }
        }
    }

    fun filterByCategory(category: Category) {
        setState { it.copy(selectedCategory = category.name) }
        // Implement category filtering
    }

    fun onRestaurantClick(restaurantId: String) {
        emitEvent(HomeEvent.NavigateToRestaurant(restaurantId))
    }

    fun retry() {
        loadRestaurants()
    }

    fun clearError() {
        setState { it.copy(error = null) }
    }


    private fun loadCategories() {
//        val categories = listOf(
//            Category("Pizza", "ic_pizza"),
//            Category("Burger", "ic_burger"),
//            Category("Sushi", "ic_sushi"),
//            Category("Mexican", "ic_mexican"),
//            Category("Asian", "ic_asian"),
//            Category("Desserts", "ic_dessert"),
//            Category("Healthy", "ic_healthy"),
//            Category("Coffee", "ic_coffee")
//        )

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
            if (locationRepository.hasLocationPermission()) {
                val location = locationRepository.getCurrentLocation()
                setState { it.copy(userLocation = location) }

                location?.let {
                    updateLocationUseCase(it, "")//TODO pass a userId
                    loadRestaurants()
                }
            } else {
                emitEvent(HomeEvent.LocationPermissionDenied)
            }
        }
    }

    private fun calculateDistance(location1: Location, location2: Location): Double {
        val earthRadius = 6371.0 // kilometers

        val lat1 = Math.toRadians(location1.latitude)
        val lon1 = Math.toRadians(location1.longitude)
        val lat2 = Math.toRadians(location2.latitude)
        val lon2 = Math.toRadians(location2.longitude)

        val dLat = lat2 - lat1
        val dLon = lon2 - lon1

        val a = sin(dLat / 2).pow(2) + cos(lat1) * cos(lat2) * sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return earthRadius * c
    }
}

//@Composable
//fun CategoryItem(category: Category) {
//    val iconId = remember(category.iconRes) {
//        val context = LocalContext.current
//        context.resources.getIdentifier(category.iconRes, "drawable", context.packageName)
//    }
//
//    Image(
//        painter = painterResource(id = iconId),
//        contentDescription = category.name
//    )
//}
