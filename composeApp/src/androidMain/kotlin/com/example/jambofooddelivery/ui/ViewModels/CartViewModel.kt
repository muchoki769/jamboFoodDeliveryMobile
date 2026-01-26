package com.example.jambofooddelivery.ui.ViewModels

import com.example.jambofooddelivery.models.Restaurant
import com.example.jambofooddelivery.repositories.CartRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import com.example.jambofooddelivery.models.MenuItem
import com.example.jambofooddelivery.models.CartItem
import kotlinx.coroutines.flow.collectLatest

data class CartState(
    val isLoading: Boolean = false,
    val cartItems: List<CartItem> = emptyList(),
    val totalAmount: Double = 0.0,
    val deliveryFee: Double = 0.0,
    val taxAmount: Double = 0.0,
    val finalAmount: Double = 0.0,
    val restaurant: Restaurant? = null,
    val error: String? = null
)

sealed class CartEvent {
    object NavigateToCheckout : CartEvent()
    data class ShowError(val message: String) : CartEvent()
    data class ItemRemoved(val itemName: String) : CartEvent()
}

class CartViewModel : BaseViewModel<CartState, CartEvent>(CartState()), KoinComponent {
    private val cartRepository: CartRepository by inject()

    init {
        observeCart()
    }

    private fun observeCart() {
        launch {
            cartRepository.getCartItems().collectLatest { items ->
                setState { it.copy(cartItems = items) }
                calculateTotals()
            }
        }
    }

    fun loadCart(restaurant: Restaurant?) {
        setState {
            it.copy(
                restaurant = restaurant,
                deliveryFee = restaurant?.deliveryFeeDouble ?: 0.0
            )
        }
        calculateTotals()
    }

    fun updateItemQuantity(itemId: String, newQuantity: Int) {
        launch {
            cartRepository.updateQuantity(itemId, newQuantity)
        }
    }


    fun removeItem(itemId: String) {
        launch {
            val itemName = state.value.cartItems.find { it.id == itemId }?.menuItem?.name
            cartRepository.removeItem(itemId)
            itemName?.let {
                emitEvent(CartEvent.ItemRemoved(it))
            }
        }
    }

    fun clearCart() {
        launch {
            cartRepository.clearCart()
        }
    }


    fun proceedToCheckout() {
        if (state.value.cartItems.isNotEmpty()) {
            emitEvent(CartEvent.NavigateToCheckout)
        }
    }

    fun applyPromoCode(code: String) {
        // Implement promo code logic
        // This would calculate discounts and update totals
    }

    private fun calculateTotals() {
        val subtotal = state.value.cartItems.sumOf { it.menuItem.price * it.quantity }
        val tax = subtotal * 0.1 // 10% tax
        val deliveryFee = state.value.deliveryFee
        val finalAmount = subtotal + tax + deliveryFee

        setState {
            it.copy(
                totalAmount = subtotal,
                taxAmount = tax,
                finalAmount = finalAmount
            )
        }
    }
}
