package com.example.jambofooddelivery.ui.ViewModels

import com.example.jambofooddelivery.models.MenuItem
import com.example.jambofooddelivery.models.Restaurant
import com.example.jambofooddelivery.repositories.RestaurantRepository
import com.example.jambofooddelivery.utils.Result
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

data class RestaurantState(
    val isLoading: Boolean = false,
    val restaurant: Restaurant? = null,
    val selectedCategory: String? = null,
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

    fun loadRestaurant(restaurantId: String) {
        launch {
            setState { it.copy(isLoading = true, error = null) }

            when (val result = restaurantRepository.getRestaurantById(restaurantId)) {
                is Result.Success -> {
                    setState {
                        it.copy(
                            isLoading = false,
                            restaurant = result.data,
                            selectedCategory = result.data.categories.firstOrNull()?.name
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
                    emitEvent(RestaurantEvent.ShowError(result.message))
                }
                 is Result.Loading -> {
                     // Already set via setState
                 }
            }
        }
    }


    fun selectCategory(categoryName: String) {
        setState { it.copy(selectedCategory = categoryName) }
    }

    fun addToCart(menuItem: MenuItem, quantity: Int = 1, specialInstructions: String? = null) {
        val currentItems = state.value.cartItems.toMutableList()

        val existingItemIndex = currentItems.indexOfFirst { it.menuItem.id == menuItem.id }

        if (existingItemIndex != -1) {
            // Update existing item
            val existingItem = currentItems[existingItemIndex]
            currentItems[existingItemIndex] = existingItem.copy(
                quantity = existingItem.quantity + quantity,
                specialInstructions = specialInstructions ?: existingItem.specialInstructions
            )
        } else {
            // Add new item
            val cartItem = CartItem(
                id = "${menuItem.id}_${System.currentTimeMillis()}",
                menuItem = menuItem,
                quantity = quantity,
                specialInstructions = specialInstructions,
                addedAt = System.currentTimeMillis()
            )
            currentItems.add(cartItem)
        }

        updateCartState(currentItems)
        emitEvent(RestaurantEvent.ItemAddedToCart(menuItem.name))
    }

    fun updateCartItemQuantity(itemId: String, newQuantity: Int) {
        if (newQuantity <= 0) {
            removeFromCart(itemId)
            return
        }

        val currentItems = state.value.cartItems.toMutableList()
        val itemIndex = currentItems.indexOfFirst { it.id == itemId }

        if (itemIndex != -1) {
            val item = currentItems[itemIndex]
            currentItems[itemIndex] = item.copy(quantity = newQuantity)
            updateCartState(currentItems)
        }
    }

    fun removeFromCart(itemId: String) {
        val currentItems = state.value.cartItems.toMutableList()
        currentItems.removeAll { it.id == itemId }
        updateCartState(currentItems)
    }

    fun clearCart() {
        setState { it.copy(cartItems = emptyList(), totalCartItems = 0, totalCartAmount = 0.0) }
    }

    fun navigateToCart() {
        if (state.value.cartItems.isNotEmpty()) {
            emitEvent(RestaurantEvent.NavigateToCart)
        }
    }

    fun clearError() {
        setState { it.copy(error = null) }
    }

    private fun updateCartState(cartItems: List<CartItem>) {
        val totalItems = cartItems.sumOf { it.quantity }
        val totalAmount = cartItems.sumOf { it.menuItem.price * it.quantity }

        setState {
            it.copy(
                cartItems = cartItems,
                totalCartItems = totalItems,
                totalCartAmount = totalAmount
            )
        }
    }
}


data class CartItem(
    val id: String,
    val menuItem: MenuItem,
    val quantity: Int,
    val specialInstructions: String?,
    val addedAt: Long
)
