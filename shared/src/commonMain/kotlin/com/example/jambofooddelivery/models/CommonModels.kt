package com.example.jambofooddelivery.models
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.datetime.Instant

@Serializable
data class User(
    val id: String,
    val email: String,
    @SerialName("first_name")
    val firstName: String,
    @SerialName("last_name")
    val lastName: String,
    val phone: String? = null,
    @SerialName("avatar_url")
    val avatarUrl: String? = null,
    val role: UserRole,
    @SerialName("is_active")
    val isActive: Boolean,
    @SerialName("created_at")
    val createdAt: Instant,
    @SerialName("updated_at")
    val updatedAt: Instant
) {
    val fullName: String
        get() = "$firstName $lastName"
}

@Serializable
enum class UserRole {
    @SerialName("customer")
    CUSTOMER,
    @SerialName("rider")
    RIDER,
    @SerialName("admin")
    ADMIN,
    @SerialName("restaurant_owner")
    RESTAURANT_OWNER
}

@Serializable
data class AuthResponse(
    val user: User,
    val token: String? = null,
    @SerialName("access_token")
    val accessToken: String? = null
)

@Serializable
data class Restaurant(
    val id: String,
    val name: String,
    val description: String,
    @SerialName("cover_image_url")
    val coverImageUrl: String? = null,
    @SerialName("logo_url")
    val logoUrl: String? = null,
    val address: Address,
    val location: Location,
    val rating: Double,
    @SerialName("delivery_time_range")
    val deliveryTimeRange: String,
    @SerialName("minimum_order")
    val minimumOrder: Double,
    @SerialName("delivery_fee")
    val deliveryFee: Double,
    @SerialName("is_active")
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
    @SerialName("image_url")
    val imageUrl: String? = null,
    val ingredients: List<String> = emptyList(),
    @SerialName("is_vegetarian")
    val isVegetarian: Boolean = false,
    @SerialName("is_spicy")
    val isSpicy: Boolean = false,
    @SerialName("is_available")
    val isAvailable: Boolean = true,
    @SerialName("preparation_time")
    val preparationTime: Int? = null
)


@Serializable
data class Order(
    val id: String,
    @SerialName("order_number")
    val orderNumber: String,
    @SerialName("customer_id")
    val customerId: String,
    @SerialName("restaurant_id")
    val restaurantId: String,
    @SerialName("rider_id")
    val riderId: String? = null,
    val status: OrderStatus,
    val items: List<OrderItem>,
    @SerialName("total_amount")
    val totalAmount: Double,
    @SerialName("delivery_fee")
    val deliveryFee: Double,
    @SerialName("tax_amount")
    val taxAmount: Double,
    @SerialName("final_amount")
    val finalAmount: Double,
    @SerialName("delivery_address")
    val deliveryAddress: Address,
    @SerialName("special_instructions")
    val specialInstructions: String? = null,
    @SerialName("estimated_delivery_time")
    val estimatedDeliveryTime: Instant? = null,
    @SerialName("actual_delivery_time")
    val actualDeliveryTime: Instant? = null,
    @SerialName("payment_status")
    val paymentStatus: PaymentStatus,
    @SerialName("payment_method")
    val paymentMethod: PaymentMethod,
    @SerialName("created_at")
    val createdAt: Instant,
    @SerialName("updated_at")
    val updatedAt: Instant
)


@Serializable
data class OrderItem(
    val id: String,
    @SerialName("menu_item_id")
    val menuItemId: String,
    @SerialName("menu_item_name")
    val menuItemName: String,
    val quantity: Int,
    @SerialName("unit_price")
    val unitPrice: Double,
    @SerialName("total_price")
    val totalPrice: Double,
    @SerialName("special_instructions")
    val specialInstructions: String? = null
)

@Serializable
data class Address(
    val street: String,
    val city: String,
    val state: String,
    @SerialName("zip_code")
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
    @SerialName("pending")
    PENDING,
    @SerialName("confirmed")
    CONFIRMED,
    @SerialName("preparing")
    PREPARING,
    @SerialName("ready")
    READY,
    @SerialName("picked_up")
    PICKED_UP,
    @SerialName("on_the_way")
    ON_THE_WAY,
    @SerialName("delivered")
    DELIVERED,
    @SerialName("cancelled")
    CANCELLED
}

@Serializable
enum class PaymentStatus {
    @SerialName("pending")
    PENDING,
    @SerialName("paid")
    PAID,
    @SerialName("failed")
    FAILED,
    @SerialName("refunded")
    REFUNDED
}

@Serializable
enum class PaymentMethod {
    @SerialName("stripe")
    STRIPE,
    @SerialName("mpesa")
    MPESA,
    @SerialName("cash")
    CASH
}

@Serializable
data class ChatMessage(
    val id: String,
    @SerialName("room_id")
    val roomId: String,
    @SerialName("sender_id")
    val senderId: String,
    @SerialName("sender_name")
    val senderName: String,
    val message: String,
    @SerialName("message_type")
    val messageType: MessageType,
    @SerialName("image_url")
    val imageUrl: String? = null,
    val timestamp: Instant,
    @SerialName("is_read")
    val isRead: Boolean
)

@Serializable
enum class MessageType {
    @SerialName("text")
    TEXT,
    @SerialName("image")
    IMAGE,
    @SerialName("system")
    SYSTEM
}

@Serializable
data class ProfileUpdate(
    @SerialName("first_name")
    val firstName: String? = null,
    @SerialName("last_name")
    val lastName: String? = null,
    val phone: String? = null,
    @SerialName("avatar_url")
    val avatarUrl: String? = null
)

@Serializable
data class CloudinaryResponse(
    val secure_url: String,
    val public_id: String,
    val version: Int
)

//@Serializable
//data class PaymentResponse(
//    val status: String,
//    @SerialName("payment_id")
//    val paymentId: String,
//    @SerialName("client_secret")
//    val clientSecret: String? = null
//)


