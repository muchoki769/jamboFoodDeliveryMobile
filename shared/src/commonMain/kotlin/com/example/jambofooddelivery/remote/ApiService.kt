package com.example.jambofooddelivery.remote
import com.example.jambofooddelivery.models.Address
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
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*


import kotlinx.serialization.Serializable

interface ApiService {
    suspend fun login(request: LoginRequest): ApiResponse<User>
    suspend fun register(request: RegisterRequest): ApiResponse<User>
    suspend fun getRestaurants(location: Location): ApiResponse<List<Restaurant>>
    suspend fun getRestaurantMenu(restaurantId: String): ApiResponse<Restaurant>
    suspend fun createOrder(request: CreateOrderRequest): ApiResponse<OrderResponse>
    suspend fun getOrder(orderId: String): ApiResponse<Order>
    suspend fun getUserOrders(userId: String): ApiResponse<List<Order>>
    suspend fun updateOrderStatus(orderId: String, status: OrderStatus): ApiResponse<Order>
    suspend fun createPaymentIntent(request: PaymentIntentRequest): ApiResponse<PaymentIntentResponse>
    suspend fun initiateMpesaPayment(request: MpesaPaymentRequest): ApiResponse<MpesaPaymentResponse>
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
}

class ApiServiceImpl(private val client: HttpClient) : ApiService {

    override suspend fun login(request: LoginRequest): ApiResponse<User> {
        return try {
            val response: ApiResponse<User> = client.post("auth/login") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()
            response
        } catch (e: Exception) {
            ApiResponse.Error(e.message ?: "Login failed")
        }
    }

    override suspend fun createOrder(request: CreateOrderRequest): ApiResponse<OrderResponse> {
        return try {
            val response: ApiResponse<OrderResponse> = client.post("orders") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()
            response
        } catch (e: Exception) {
            ApiResponse.Error(e.message ?: "Order creation failed")
        }
    }

    override suspend fun register(request: RegisterRequest): ApiResponse<User> {
        return try {
            val response: ApiResponse<User> = client.post("auth/register") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()
            response
        } catch (e: Exception) {
            ApiResponse.Error(e.message ?: "Registration failed")
        }
    }

    override suspend fun getRestaurants(location: Location): ApiResponse<List<Restaurant>> {
         return try {
            val response: ApiResponse<List<Restaurant>> = client.get("restaurants") {
                parameter("lat", location.latitude)
                parameter("lng", location.longitude)
            }.body()
            response
        } catch (e: Exception) {
            ApiResponse.Error(e.message ?: "Failed to fetch restaurants")
        }
    }

    override suspend fun getRestaurantMenu(restaurantId: String): ApiResponse<Restaurant> {
        return try {
            val response: ApiResponse<Restaurant> = client.get("restaurants/$restaurantId").body()
            response
        } catch (e: Exception) {
            ApiResponse.Error(e.message ?: "Failed to fetch menu")
        }
    }

    override suspend fun getOrder(orderId: String): ApiResponse<Order> {
         return try {
            val response: ApiResponse<Order> = client.get("orders/$orderId").body()
            response
        } catch (e: Exception) {
            ApiResponse.Error(e.message ?: "Failed to fetch order")
        }
    }

    override suspend fun getUserOrders(userId: String): ApiResponse<List<Order>> {
        return try {
            val response: ApiResponse<List<Order>> = client.get("users/$userId/orders").body()
            response
        } catch (e: Exception) {
            ApiResponse.Error(e.message ?: "Failed to fetch user orders")
        }
    }

    override suspend fun updateOrderStatus(orderId: String, status: OrderStatus): ApiResponse<Order> {
         return try {
            val response: ApiResponse<Order> = client.put("orders/$orderId/status") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("status" to status))
            }.body()
            response
        } catch (e: Exception) {
            ApiResponse.Error(e.message ?: "Failed to update order status")
        }
    }

    override suspend fun createPaymentIntent(request: PaymentIntentRequest): ApiResponse<PaymentIntentResponse> {
        return try {
            val response: ApiResponse<PaymentIntentResponse> = client.post("payments/intent") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()
            response
        } catch (e: Exception) {
            ApiResponse.Error(e.message ?: "Failed to create payment intent")
        }
    }

    override suspend fun initiateMpesaPayment(request: MpesaPaymentRequest): ApiResponse<MpesaPaymentResponse> {
         return try {
            val response: ApiResponse<MpesaPaymentResponse> = client.post("payments/mpesa") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()
            response
        } catch (e: Exception) {
            ApiResponse.Error(e.message ?: "Failed to initiate M-Pesa payment")
        }
    }

    override suspend fun getChatRooms(userId: String): ApiResponse<List<ChatRoom>> {
        return try {
            val response: ApiResponse<List<ChatRoom>> = client.get("users/$userId/chats").body()
            response
        } catch (e: Exception) {
            ApiResponse.Error(e.message ?: "Failed to fetch chat rooms")
        }
    }

    override suspend fun getChatMessages(roomId: String): ApiResponse<List<ChatMessage>> {
        return try {
            val response: ApiResponse<List<ChatMessage>> = client.get("chats/$roomId/messages").body()
            response
        } catch (e: Exception) {
            ApiResponse.Error(e.message ?: "Failed to fetch chat messages")
        }
    }

    override suspend fun sendMessage(request: SendMessageRequest): ApiResponse<ChatMessage> {
        return try {
            val response: ApiResponse<ChatMessage> = client.post("chats/${request.roomId}/messages") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()
            response
        } catch (e: Exception) {
            ApiResponse.Error(e.message ?: "Failed to send message")
        }
    }

    override suspend fun updateLocation(userId: String, location: Location): ApiResponse<Unit> {
        return try {
             client.put("users/$userId/location") {
                contentType(ContentType.Application.Json)
                setBody(location)
            }
            ApiResponse.Success(Unit)
        } catch (e: Exception) {
            ApiResponse.Error(e.message ?: "Failed to update location")
        }
    }
    
    override suspend fun updateProfile(request: ProfileUpdate): ApiResponse<User> {
        return try {
            val response: ApiResponse<User> = client.put("users/profile") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()
            response
        } catch (e: Exception) {
            ApiResponse.Error(e.message ?: "Failed to update profile")
        }
    }

    override suspend fun updateUser(userId: String, user: User): ApiResponse<User> {
        return try {
            val response: ApiResponse<User> = client.put("users/$userId") {
                contentType(ContentType.Application.Json)
                setBody(user)
            }.body()
            response
        } catch (e: Exception) {
            ApiResponse.Error(e.message ?: "Failed to update user")
        }
    }

    override suspend fun geocodeAddress(address: String): ApiResponse<Location> {
        return try {
            val response: ApiResponse<Location> = client.get("location/geocode") {
                parameter("address", address)
            }.body()
            response
        } catch (e: Exception) {
             ApiResponse.Error(e.message ?: "Geocoding failed")
        }
    }

    override suspend fun reverseGeocode(location: Location): ApiResponse<String> {
        return try {
            val response: ApiResponse<String> = client.get("location/reverse-geocode") {
                 parameter("lat", location.latitude)
                parameter("lng", location.longitude)
            }.body()
            response
        } catch (e: Exception) {
            ApiResponse.Error(e.message ?: "Reverse geocoding failed")
        }
    }
    
    override suspend fun getOrCreateSupportRoom(userId: String, orderId: String?): ApiResponse<ChatRoom> {
         return try {
            val response: ApiResponse<ChatRoom> = client.post("chats/support") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("userId" to userId, "orderId" to orderId))
            }.body()
            response
        } catch (e: Exception) {
            ApiResponse.Error(e.message ?: "Failed to create support room")
        }
    }
    
    override suspend fun markMessagesAsRead(roomId: String): ApiResponse<Unit> {
         return try {
             client.put("chats/$roomId/read") 
             ApiResponse.Success(Unit)
        } catch (e: Exception) {
            ApiResponse.Error(e.message ?: "Failed to mark messages as read")
        }
    }

    override suspend fun searchRestaurants(query: String, location: Location): ApiResponse<List<Restaurant>> {
        return try {
            val response: ApiResponse<List<Restaurant>> = client.get("restaurants/search") {
                parameter("query", query)
                parameter("lat", location.latitude)
                parameter("lng", location.longitude)
            }.body()
            response
        } catch (e: Exception) {
            ApiResponse.Error(e.message ?: "Failed to search restaurants")
        }
    }

    override suspend fun getNearbyRestaurants(location: Location, radius: Int): ApiResponse<List<Restaurant>> {
        return try {
            val response: ApiResponse<List<Restaurant>> = client.get("restaurants/nearby") {
                parameter("lat", location.latitude)
                parameter("lng", location.longitude)
                parameter("radius", radius)
            }.body()
            response
        } catch (e: Exception) {
            ApiResponse.Error(e.message ?: "Failed to fetch nearby restaurants")
        }
    }
}

@Serializable
data class ApiResponse<T>(
    val success: Boolean,
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
    val firstName: String,
    val lastName: String,
    val phone: String
)

@Serializable
data class CreateOrderRequest(
    val restaurantId: String,
    val items: List<OrderItemRequest>,
    val deliveryAddress: Address,
    val paymentMethod: PaymentMethod,
    val specialInstructions: String? = null,
    val mpesaPhone: String? = null
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
data class SendMessageRequest(
    val roomId: String,
    val message: String,
    val messageType: MessageType = MessageType.TEXT,
    val imageUrl: String? = null
)

@Serializable
data class ProfileUpdate(
    val firstName: String? = null,
    val lastName: String? = null,
    val phone: String? = null,
    val avatarUrl: String? = null
)