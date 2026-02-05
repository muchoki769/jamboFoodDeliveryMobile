package com.jambofooddelivery.repositories

import com.jambofooddelivery.cache.Database
import com.jambofooddelivery.models.MenuItem
import com.jambofooddelivery.models.CartItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.Clock

interface CartRepository {
    fun getCartItems(): Flow<List<CartItem>>
    suspend fun addItem(restaurantId: String, menuItem: MenuItem, quantity: Int = 1)
    suspend fun updateQuantity(itemId: String, quantity: Int)
    suspend fun removeItem(itemId: String)
    suspend fun clearCart()
}

class CartRepositoryImpl(private val database: Database) : CartRepository {
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    
    init {
        loadItems()
    }

    override fun getCartItems(): Flow<List<CartItem>> = _cartItems.asStateFlow()

    private fun loadItems() {
        val items = database.cartQueries.getCartItems().executeAsList().map {
            CartItem(
                id = it.id,
                restaurantId = it.restaurant_id,
                menuItem = MenuItem(
                    id = it.menu_item_id,
                    name = it.name,
                    description = it.description,
                    price = it.price,
                    imageUrl = it.image_url
                ),
                quantity = it.quantity.toInt(),
                specialInstructions = it.special_instructions,
                addedAt = it.added_at
            )
        }
        _cartItems.value = items
    }

    override suspend fun addItem(restaurantId: String, menuItem: MenuItem, quantity: Int) {
        val existingItem = _cartItems.value.find { it.menuItem.id == menuItem.id }
        if (existingItem != null) {
            updateQuantity(existingItem.id, existingItem.quantity + quantity)
        } else {
            val now = Clock.System.now().toEpochMilliseconds()
            val id = "${menuItem.id}_$now"
            database.cartQueries.insertCartItem(
                id = id,
                restaurant_id = restaurantId,
                menu_item_id = menuItem.id,
                name = menuItem.name,
                description = menuItem.description,
                price = menuItem.price,
                image_url = menuItem.imageUrl,
                quantity = quantity.toLong(),
                special_instructions = null,
                added_at = now
            )
            loadItems()
        }
    }

    override suspend fun updateQuantity(itemId: String, quantity: Int) {
        if (quantity <= 0) {
            removeItem(itemId)
        } else {
            val item = database.cartQueries.getCartItems().executeAsList().find { it.id == itemId }
            item?.let {
                database.cartQueries.insertCartItem(
                    id = it.id,
                    restaurant_id = it.restaurant_id,
                    menu_item_id = it.menu_item_id,
                    name = it.name,
                    description = it.description,
                    price = it.price,
                    image_url = it.image_url,
                    quantity = quantity.toLong(),
                    special_instructions = it.special_instructions,
                    added_at = it.added_at
                )
            }
            loadItems()
        }
    }

    override suspend fun removeItem(itemId: String) {
        database.cartQueries.removeItem(itemId)
        loadItems()
    }

    override suspend fun clearCart() {
        database.cartQueries.clearCart()
        loadItems()
    }
}
