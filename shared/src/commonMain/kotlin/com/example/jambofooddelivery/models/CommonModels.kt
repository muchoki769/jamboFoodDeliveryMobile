package com.example.jambofooddelivery.models
import kotlinx.serialization.Serializable
import kotlinx.datetime.Instant

@Serializable
data class User(
    val id: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val phone: String? = null,
    val avatarUrl: String? = null,
    val role: UserRole,
    val isActive: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    val fullName: String
        get() = "$firstName $lastName"
}

@Serializable
enum class UserRole {
    CUSTOMER, RIDER, ADMIN, RESTAURANT_OWNER
}

@Serializable
data class Restaurant(
    val id: String,
    val name: String,
    val description: String,
    val coverImageUrl: String? = null,
    val logoUrl: String? = null,
    val address: Address,
    val location: Location,
    val rating: Double,
    val deliveryTimeRange: String,
    val minimumOrder: Double,
    val deliveryFee: Double,
    val isActive: Boolean,
    val categories: List<MenuCategory> = emptyList()
)

@Serializable
data class MenuCategory(
    val id: String,
    val name: String,
    val description: String? = null,
    val items: List<MenuItem> = emptyList()
)

@Serializable
data class MenuItem(
    val id: String,
    val name: String,
    val description: String? = null,
    val price: Double,
    val imageUrl: String? = null,
    val ingredients: List<String> = emptyList(),
    val isVegetarian: Boolean = false,
    val isSpicy: Boolean = false,
    val isAvailable: Boolean = true,
    val preparationTime: Int? = null
)


@Serializable
data class Order(
    val id: String,
    val orderNumber: String,
    val customerId: String,
    val restaurantId: String,
    val riderId: String? = null,
    val status: OrderStatus,
    val items: List<OrderItem>,
    val totalAmount: Double,
    val deliveryFee: Double,
    val taxAmount: Double,
    val finalAmount: Double,
    val deliveryAddress: Address,
    val specialInstructions: String? = null,
    val estimatedDeliveryTime: Instant? = null,
    val actualDeliveryTime: Instant? = null,
    val paymentStatus: PaymentStatus,
    val paymentMethod: PaymentMethod,
    val createdAt: Instant,
    val updatedAt: Instant
)


@Serializable
data class OrderItem(
    val id: String,
    val menuItemId: String,
    val menuItemName: String,
    val quantity: Int,
    val unitPrice: Double,
    val totalPrice: Double,
    val specialInstructions: String? = null
)

@Serializable
data class Address(
    val street: String,
    val city: String,
    val state: String,
    val zipCode: String,
    val country: String,
    val latitude: Double,
    val longitude: Double
) {
    val fullAddress: String
        get() = "$street, $city, $state $zipCode"
}

@Serializable
data class Location(
    val latitude: Double,
    val longitude: Double
)

@Serializable
enum class OrderStatus {
    PENDING, CONFIRMED, PREPARING, READY, PICKED_UP, ON_THE_WAY, DELIVERED, CANCELLED
}

@Serializable
enum class PaymentStatus {
    PENDING, PAID, FAILED, REFUNDED
}

@Serializable
enum class PaymentMethod {
    STRIPE, MPESA, CASH
}

@Serializable
data class ChatMessage(
    val id: String,
    val roomId: String,
    val senderId: String,
    val senderName: String,
    val message: String,
    val messageType: MessageType,
    val imageUrl: String? = null,
    val timestamp: Instant,
    val isRead: Boolean
)

@Serializable
enum class MessageType {
    TEXT, IMAGE, SYSTEM
}

@Serializable
data class ProfileUpdate(
    val firstName: String? = null,
    val lastName: String? = null,
    val phone: String? = null,
    val avatarUrl: String? = null
)

@Serializable
data class CloudinaryResponse(
    val secure_url: String,
    val public_id: String,
    val version: Int
)
