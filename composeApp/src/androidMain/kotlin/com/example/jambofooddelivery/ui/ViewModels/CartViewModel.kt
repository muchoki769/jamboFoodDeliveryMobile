package com.example.jambofooddelivery.ui.ViewModels

import com.example.jambofooddelivery.models.Restaurant
import org.koin.core.component.KoinComponent

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
    fun loadCart(restaurant: Restaurant?) {
        // In a real app, you'd load from local database
        setState {
            it.copy(
                restaurant = restaurant,
                deliveryFee = restaurant?.deliveryFeeDouble ?: 0.0
            )
        }
        calculateTotals()
    }

    fun updateItemQuantity(itemId: String, newQuantity: Int) {
        if (newQuantity <= 0) {
            removeItem(itemId)
            return
        }
        val currentItems = state.value.cartItems.toMutableList()
        val itemIndex = currentItems.indexOfFirst { it.id == itemId }

        if (itemIndex != -1) {
            val item = currentItems[itemIndex]
            currentItems[itemIndex] = item.copy(quantity = newQuantity)
            setState { it.copy(cartItems = currentItems) }
            calculateTotals()
        }
    }


    fun removeItem(itemId: String) {
        val currentItems = state.value.cartItems.toMutableList()
        val removedItem = currentItems.find { it.id == itemId }
        currentItems.removeAll { it.id == itemId }

        setState { it.copy(cartItems = currentItems) }
        calculateTotals()

        removedItem?.let {
            emitEvent(CartEvent.ItemRemoved(it.menuItem.name))
        }
    }

    fun clearCart() {
        setState {
            it.copy(
                cartItems = emptyList(),
                totalAmount = 0.0,
                deliveryFee = 0.0,
                taxAmount = 0.0,
                finalAmount = 0.0
            )
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

