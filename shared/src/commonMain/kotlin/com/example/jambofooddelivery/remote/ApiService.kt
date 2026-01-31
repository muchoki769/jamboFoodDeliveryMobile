package com.example.jambofooddelivery.remote
import com.example.jambofooddelivery.models.Address
import com.example.jambofooddelivery.models.AuthResponse
import com.example.jambofooddelivery.models.ChatMessage
import com.example.jambofooddelivery.models.ChatRoom
import com.example.jambofooddelivery.models.Location
import com.example.jambofooddelivery.models.MessageType
import com.example.jambofooddelivery.models.Order
import com.example.jambofooddelivery.models.OrderStatus
import com.example.jambofooddelivery.models.PaymentMethod
import com.example.jambofooddelivery.models.PaymentResponse
import com.example.jambofooddelivery.models.Restaurant
import com.example.jambofooddelivery.models.User
import com.example.jambofooddelivery.models.MenuItem
import com.example.jambofooddelivery.models.MenuCategory
import com.example.jambofooddelivery.models.Notification
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*


import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.json.Json

interface ApiService {
    suspend fun login(request: LoginRequest): ApiResponse<AuthResponse>
    suspend fun register(request: RegisterRequest): ApiResponse<AuthResponse>
    suspend fun getRestaurants(location: Location): ApiResponse<List<Restaurant>>
    suspend fun getRestaurantMenu(restaurantId: String): ApiResponse<Restaurant>
    suspend fun createOrder(request: CreateOrderRequest): ApiResponse<OrderResponse>
    suspend fun getOrder(orderId: String): ApiResponse<Order>
    suspend fun getUserOrders(userId: String): ApiResponse<List<Order>>
    suspend fun updateOrderStatus(orderId: String, status: OrderStatus): ApiResponse<Order>
    suspend fun createPaymentIntent(request: PaymentIntentRequest): ApiResponse<PaymentIntentResponse>
    suspend fun initiateMpesaPayment(request: MpesaPaymentRequest): ApiResponse<MpesaPaymentResponse>
    suspend fun getMpesaStatus(checkoutRequestId: String): ApiResponse<MpesaStatusResponse>
    suspend fun getChatRooms(userId: String): ApiResponse<List<ChatRoom>>
    suspend fun getChatMessages(roomId: String): ApiResponse<List<ChatMessage>>
    suspend fun sendMessage(request: SendMessageRequest): ApiResponse<ChatMessage>
    suspend fun updateLocation(userId: String, location: Location): ApiResponse<Unit>
    suspend fun updateProfile(request: ProfileUpdate): ApiResponse<User>
    suspend fun updateUser(userId: String, user: User): ApiResponse<User>
    suspend fun geocodeAddress(address: String): ApiResponse<Location>
    suspend fun reverseGeocode(location: Location): ApiResponse<String>
    suspend fun getOrCreateSupportRoom(userId: String, orderId: String?): ApiResponse<ChatRoom>
    suspend fun markMessagesAsRead(roomId: String): ApiResponse<Unit>
    suspend fun searchRestaurants(query: String, location: Location): ApiResponse<List<Restaurant>>
    suspend fun getNearbyRestaurants(location: Location, radius: Int): ApiResponse<List<Restaurant>>
    
    // Menu specific routes based on backend
    suspend fun getRestaurantCategories(restaurantId: String): ApiResponse<List<MenuCategory>>
    suspend fun getCategoryItems(categoryId: String): ApiResponse<List<MenuItem>>
    suspend fun getPopularItems(restaurantId: String): ApiResponse<List<MenuItem>>

    // Notification routes
    suspend fun getNotifications(userId: String): ApiResponse<List<Notification>>
    suspend fun markNotificationAsRead(notificationId: String): ApiResponse<Unit>
    suspend fun registerFcmToken(userId: String, token: String): ApiResponse<Unit>
}

class ApiServiceImpl(private val client: HttpClient) : ApiService {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    private suspend inline fun <reified T> safeRequest(block: () -> HttpResponse): ApiResponse<T> {
        return try {
            val response = block()
            val text = response.bodyAsText()
            if (response.status.isSuccess()) {
                // If the backend returns the raw data directly instead of wrapping it in ApiResponse
                try {
                    val data = json.decodeFromString<T>(text)
                    ApiResponse.Success(data)
                } catch (e: Exception) {
                    // Try decoding as ApiResponse if direct decoding fails
                    val apiResponse = json.decodeFromString<ApiResponse<T>>(text)
                    if (apiResponse.success) {
                        apiResponse
                    } else {
                        ApiResponse.Error(apiResponse.error ?: apiResponse.message ?: "Unknown error")
                    }
                }
            } else {
                try {
                    val apiResponse = json.decodeFromString<ApiResponse<T>>(text)
                    ApiResponse.Error(apiResponse.error ?: apiResponse.message ?: "Server error: ${response.status.value}")
                } catch (e: Exception) {
                    ApiResponse.Error("Server error: ${response.status.value}")
                }
            }
        } catch (e: Exception) {
            ApiResponse.Error(e.message ?: "Network error")
        }
    }

    override suspend fun login(request: LoginRequest): ApiResponse<AuthResponse> = safeRequest {
        client.post("auth/login") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    override suspend fun createOrder(request: CreateOrderRequest): ApiResponse<OrderResponse> = safeRequest {
        client.post("orders") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    override suspend fun register(request: RegisterRequest): ApiResponse<AuthResponse> = safeRequest {
        client.post("auth/register") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    override suspend fun getRestaurants(location: Location): ApiResponse<List<Restaurant>> = safeRequest {
        client.get("restaurants") {
            parameter("lat", location.latitude)
            parameter("lng", location.longitude)
            parameter("radius", 500000) //within 5km
        }
    }

    override suspend fun getRestaurantMenu(restaurantId: String): ApiResponse<Restaurant> = safeRequest {
        client.get("restaurants/$restaurantId")
    }

    override suspend fun getOrder(orderId: String): ApiResponse<Order> = safeRequest {
        client.get("orders/$orderId")
    }

    override suspend fun getUserOrders(userId: String): ApiResponse<List<Order>> = safeRequest {
        client.get("orders/user")
    }

    override suspend fun updateOrderStatus(orderId: String, status: OrderStatus): ApiResponse<Order> = safeRequest {
        client.put("orders/$orderId/status") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("status" to status))
        }
    }

    override suspend fun createPaymentIntent(request: PaymentIntentRequest): ApiResponse<PaymentIntentResponse> = safeRequest {
        client.post("payments/stripe/initiate") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    override suspend fun initiateMpesaPayment(request: MpesaPaymentRequest): ApiResponse<MpesaPaymentResponse> = safeRequest {
        client.post("payments/mpesa/initiate") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    override suspend fun getMpesaStatus(checkoutRequestId: String): ApiResponse<MpesaStatusResponse> = safeRequest {
        client.get("payments/mpesa/callback") {
            parameter("checkoutRequestId", checkoutRequestId)
        }
    }

    override suspend fun getChatRooms(userId: String): ApiResponse<List<ChatRoom>> = safeRequest {
        client.get("users/$userId/chats")
    }

    override suspend fun getChatMessages(roomId: String): ApiResponse<List<ChatMessage>> = safeRequest {
        client.get("chats/$roomId/messages")
    }

    override suspend fun sendMessage(request: SendMessageRequest): ApiResponse<ChatMessage> = safeRequest {
        client.post("chats/${request.roomId}/messages") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    override suspend fun updateLocation(userId: String, location: Location): ApiResponse<Unit> = safeRequest {
        client.put("users/$userId/location") {
            contentType(ContentType.Application.Json)
            setBody(location)
        }
    }
    
    override suspend fun updateProfile(request: ProfileUpdate): ApiResponse<User> = safeRequest {
        client.put("users/profile") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    override suspend fun updateUser(userId: String, user: User): ApiResponse<User> = safeRequest {
        client.put("users/$userId") {
            contentType(ContentType.Application.Json)
            setBody(user)
        }
    }

    override suspend fun geocodeAddress(address: String): ApiResponse<Location> = safeRequest {
        client.get("location/geocode") {
            parameter("address", address)
        }
    }

    override suspend fun reverseGeocode(location: Location): ApiResponse<String> = safeRequest {
        client.get("location/reverse-geocode") {
            parameter("lat", location.latitude)
            parameter("lng", location.longitude)
        }
    }
    
    override suspend fun getOrCreateSupportRoom(userId: String, orderId: String?): ApiResponse<ChatRoom> = safeRequest {
        client.post("chats/support") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("userId" to userId, "orderId" to orderId))
        }
    }
    
    override suspend fun markMessagesAsRead(roomId: String): ApiResponse<Unit> = safeRequest {
        client.put("chats/$roomId/read")
    }

    override suspend fun searchRestaurants(query: String, location: Location): ApiResponse<List<Restaurant>> = safeRequest {
        client.get("restaurants/search") {
            parameter("query", query)
            parameter("lat", location.latitude)
            parameter("lng", location.longitude)
        }
    }

    override suspend fun getNearbyRestaurants(location: Location, radius: Int): ApiResponse<List<Restaurant>> = safeRequest {
        client.get("restaurants/nearby") {
            parameter("lat", location.latitude)
            parameter("lng", location.longitude)
            parameter("radius", radius)
        }
    }

    override suspend fun getRestaurantCategories(restaurantId: String): ApiResponse<List<MenuCategory>> = safeRequest {
        client.get("menu/restaurants/$restaurantId/categories")
    }

    override suspend fun getCategoryItems(categoryId: String): ApiResponse<List<MenuItem>> = safeRequest {
        client.get("menu/categories/$categoryId/items")
    }

    override suspend fun getPopularItems(restaurantId: String): ApiResponse<List<MenuItem>> = safeRequest {
        client.get("menu/restaurants/$restaurantId/popular")
    }

    override suspend fun getNotifications(userId: String): ApiResponse<List<Notification>> = safeRequest {
        client.get("users/$userId/notifications")
    }

    override suspend fun markNotificationAsRead(notificationId: String): ApiResponse<Unit> = safeRequest {
        client.put("notifications/$notificationId/read")
    }

    override suspend fun registerFcmToken(userId: String, token: String): ApiResponse<Unit> = safeRequest {
        client.post("users/$userId/fcm-token") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("token" to token))
        }
    }
}

@Serializable
data class ApiResponse<T>(
    val success: Boolean = true,
    val data: T? = null,
    val error: String? = null,
    val message: String? = null
) {
    companion object {
        fun <T> Success(data: T): ApiResponse<T> = ApiResponse(true, data)
        fun <T> Error(message: String): ApiResponse<T> = ApiResponse(false, error = message)
    }
}

@Serializable
data class LoginRequest(val email: String, val password: String)

@Serializable
data class RegisterRequest(
    val email: String,
    val password: String,
    @SerialName("first_name")
    val firstName: String,
    @SerialName("last_name")
    val lastName: String,
    val phone: String
)

@Serializable
data class DeliveryAddressRequest(
    @SerialName("lat")
    val latitude: Double,
    @SerialName("lng")
    val longitude: Double,
    val address: String,
    val instructions: String? = null
)

@Serializable
data class CreateOrderRequest(
    val customerId: String,
    val restaurantId: String,
    val items: List<OrderItemRequest>,
    val deliveryAddress: DeliveryAddressRequest,
    val paymentMethod: PaymentMethod,
    val mpesaPhone: String? = null,
    val specialInstructions: String? = null,
    val tip: Double? = null
)

@Serializable
data class OrderItemRequest(
    val menuItemId: String,
    val quantity: Int,
    val specialInstructions: String? = null
)

@Serializable
data class OrderResponse(
    val order: Order,
    val payment: PaymentResponse? = null
)

@Serializable
data class PaymentIntentRequest(
    val orderId: String,
    val amount: Double,
    val currency: String = "usd"
)

@Serializable
data class PaymentIntentResponse(
    val clientSecret: String,
    val paymentIntentId: String
)

@Serializable
data class MpesaPaymentRequest(
    val orderId: String,
    val phone: String,
    val amount: Double
)

@Serializable
data class MpesaPaymentResponse(
    val checkoutRequestId: String,
    val customerMessage: String
)

@Serializable
data class MpesaStatusResponse(
    val status: String,
    val message: String? = null
)

@Serializable
data class SendMessageRequest(
    val roomId: String,
    val message: String,
    val messageType: MessageType = MessageType.TEXT,
    val imageUrl: String? = null
)

@Serializable
data class ProfileUpdate(
    @SerialName("first_name")
    val firstName: String? = null,
    @SerialName("last_name")
    val lastName: String? = null,
    val phone: String? = null,
    val avatarUrl: String? = null
)
