package com.jambofooddelivery.Ui.ViewModels

import android.util.Log
import com.jambofooddelivery.models.MenuItem
import com.jambofooddelivery.models.Restaurant
import com.jambofooddelivery.models.CartItem
import com.jambofooddelivery.repositories.CartRepository
import com.jambofooddelivery.repositories.RestaurantRepository
import com.jambofooddelivery.utils.Result
import kotlinx.coroutines.flow.collectLatest
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

data class RestaurantState(
    val isLoading: Boolean = false,
    val isItemsLoading: Boolean = false,
    val restaurant: Restaurant? = null,
    val selectedCategoryId: String? = null,
    val selectedCategoryName: String? = null,
    val selectedCategoryDescription: String? = null,
    val categoryItems: Map<String, List<MenuItem>> = emptyMap(),
    val cartItems: List<CartItem> = emptyList(),
    val totalCartItems: Int = 0,
    val totalCartAmount: Double = 0.0,
    val error: String? = null
)

sealed class RestaurantEvent {
    object NavigateToCart : RestaurantEvent()
    data class ShowError(val message: String) : RestaurantEvent()
    data class ItemAddedToCart(val itemName: String) : RestaurantEvent()
}

class RestaurantViewModel : BaseViewModel<RestaurantState, RestaurantEvent>(RestaurantState()), KoinComponent {

    private val restaurantRepository: RestaurantRepository by inject()
    private val cartRepository: CartRepository by inject()

    init {
        observeCart()
    }

    private fun observeCart() {
        launch {
            cartRepository.getCartItems().collectLatest { items ->
                setState {
                    it.copy(
                        cartItems = items,
                        totalCartItems = items.sumOf { item -> item.quantity },
                        totalCartAmount = items.sumOf { item -> item.menuItem.price * item.quantity }
                    )
                }
            }
        }
    }

    fun loadRestaurant(restaurantId: String) {
        launch {
            Log.d("RestaurantViewModel", "Loading restaurant: $restaurantId")
            setState { it.copy(isLoading = true, error = null) }

            when (val result = restaurantRepository.getRestaurantById(restaurantId)) {
                is Result.Success -> {
                    val restaurant = result.data
                    setState { it.copy(restaurant = restaurant) }
                    
                    if (restaurant.categories.isEmpty()) {
                        Log.d("RestaurantViewModel", "Categories empty in restaurant object, fetching from API...")
                        fetchCategories(restaurantId)
                    } else {
                        val firstCategory = restaurant.categories.firstOrNull()
                        if (firstCategory != null) {
                            setState { 
                                it.copy(
                                    selectedCategoryId = firstCategory.id,
                                    selectedCategoryName = firstCategory.name,
                                    selectedCategoryDescription = firstCategory.description
                                ) 
                            }
                            loadCategoryItems(firstCategory.id)
                        }
                        setState { it.copy(isLoading = false) }
                    }
                }
                is Result.Error -> {
                    Log.e("RestaurantViewModel", "Error loading restaurant: ${result.message}")
                    setState { it.copy(isLoading = false, error = result.message) }
                    emitEvent(RestaurantEvent.ShowError(result.message))
                }
                else -> {
                    setState { it.copy(isLoading = false) }
                }
            }
        }
    }

    private suspend fun fetchCategories(restaurantId: String) {
        when (val result = restaurantRepository.getRestaurantCategories(restaurantId)) {
            is Result.Success -> {
                val categories = result.data
                Log.d("RestaurantViewModel", "Categories fetched successfully: ${categories.size}")
                
                if (categories.isNotEmpty()) {
                    val firstCategory = categories.first()
                    setState { 
                        it.copy(
                            isLoading = false,
                            restaurant = it.restaurant?.copy(categories = categories),
                            selectedCategoryId = firstCategory.id,
                            selectedCategoryName = firstCategory.name,
                            selectedCategoryDescription = firstCategory.description
                        ) 
                    }
                    loadCategoryItems(firstCategory.id)
                } else {
                    setState { it.copy(isLoading = false) }
                }
            }
            is Result.Error -> {
                Log.e("RestaurantViewModel", "Error fetching categories: ${result.message}")
                setState { it.copy(isLoading = false, error = result.message) }
                emitEvent(RestaurantEvent.ShowError(result.message))
            }
            else -> {
                setState { it.copy(isLoading = false) }
            }
        }
    }

    fun selectCategory(categoryId: String, categoryName: String) {
        if (state.value.selectedCategoryId == categoryId) return
        
        val description = state.value.restaurant?.categories?.find { it.id == categoryId }?.description
        
        setState { it.copy(
            selectedCategoryId = categoryId, 
            selectedCategoryName = categoryName,
            selectedCategoryDescription = description
        ) }
        
        if (state.value.categoryItems[categoryId].isNullOrEmpty()) {
            loadCategoryItems(categoryId)
        }
    }

    private fun loadCategoryItems(categoryId: String) {
        launch {
            Log.d("RestaurantViewModel", "Calling getCategoryItems for category: $categoryId")
            setState { it.copy(isItemsLoading = true) }
            
            when (val result = restaurantRepository.getCategoryItems(categoryId)) {
                is Result.Success -> {
                    Log.d("RestaurantViewModel", "Items loaded for $categoryId: ${result.data.size}")
                    val newMap = state.value.categoryItems.toMutableMap()
                    newMap[categoryId] = result.data
                    setState {
                        it.copy(
                            isItemsLoading = false,
                            categoryItems = newMap
                        )
                    }
                }
                is Result.Error -> {
                    Log.e("RestaurantViewModel", "Error loading items: ${result.message}")
                    setState { it.copy(isItemsLoading = false) }
                    emitEvent(RestaurantEvent.ShowError(result.message))
                }
                else -> setState { it.copy(isItemsLoading = false) }
            }
        }
    }

    fun addToCart(menuItem: MenuItem) {
        launch {
            val restaurantId = state.value.restaurant?.id ?: return@launch
            cartRepository.addItem(restaurantId, menuItem)
            emitEvent(RestaurantEvent.ItemAddedToCart(menuItem.name))
        }
    }

    fun updateCartItemQuantity(itemId: String, newQuantity: Int) {
        launch {
            cartRepository.updateQuantity(itemId, newQuantity)
        }
    }

    fun removeFromCart(itemId: String) {
        launch {
            cartRepository.removeItem(itemId)
        }
    }
}
