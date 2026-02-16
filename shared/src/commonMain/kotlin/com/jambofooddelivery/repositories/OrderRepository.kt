package com.jambofooddelivery.repositories

import com.jambofooddelivery.models.Address
import com.jambofooddelivery.models.Order
import com.jambofooddelivery.models.OrderStatus
import com.jambofooddelivery.models.PaymentMethod
import com.jambofooddelivery.preferences.AppSettings
import com.jambofooddelivery.remote.ApiService
import com.jambofooddelivery.remote.CreateOrderRequest
import com.jambofooddelivery.remote.DeliveryAddressRequest
import com.jambofooddelivery.remote.OrderItemRequest
import com.jambofooddelivery.remote.OrderResponse
import com.jambofooddelivery.remote.SocketService
import kotlinx.coroutines.flow.Flow
import com.jambofooddelivery.utils.Result
import io.github.aakira.napier.Napier

interface OrderRepository {
    suspend fun createOrder(
        restaurantId: String,
        items: List<OrderItemRequest>,
        deliveryAddress: Address,
        paymentMethod: PaymentMethod,
        mpesaPhone: String? = null,
        specialInstructions: String? = null,
        tip: Double? = null
    ): Result<OrderResponse>
    suspend fun getOrder(orderId: String): Result<Order>
    suspend fun getUserOrders(userId: String): Result<List<Order>>
    suspend fun cancelOrder(orderId: String): Result<Order>
    suspend fun trackOrder(orderId: String): Flow<Order>
    suspend fun updateOrderStatus(orderId: String, status: OrderStatus): Result<Order>
}

class OrderRepositoryImpl(
    private val apiService: ApiService,
    private val socketService: SocketService,
    private val appSettings: AppSettings
) : OrderRepository {
    override suspend fun createOrder(
        restaurantId: String,
        items: List<OrderItemRequest>,
        deliveryAddress: Address,
        paymentMethod: PaymentMethod,
        mpesaPhone: String?,
        specialInstructions: String?,
        tip: Double?
    ): Result<OrderResponse> {
        return try {
            val currentUser = appSettings.getCurrentUser()
            val customerId = currentUser?.id ?: throw Exception("User not logged in")

            val request = CreateOrderRequest(
                customerId = customerId,
                restaurantId = restaurantId,
                items = items,
                deliveryAddress = DeliveryAddressRequest(
                    latitude = deliveryAddress.latitude ?: 0.0,
                    longitude = deliveryAddress.longitude ?: 0.0,
                    address = deliveryAddress.fullAddress,
                    instructions = deliveryAddress.instructions
                ),
                paymentMethod = paymentMethod,
                mpesaPhone = mpesaPhone,
                specialInstructions = specialInstructions,
                tip = tip
            )

            Napier.d("OrderRepository: Sending CreateOrderRequest to API: $request")

            val response = apiService.createOrder(request)
            if (response.success && response.data != null) {
                Napier.d("OrderRepository: API Success response: ${response.data}")
                Result.Success(response.data)
            } else {
                Napier.e("OrderRepository: API Error response: success=${response.success}, error=${response.error}, message=${response.message}")
                Result.Error(response.error ?: response.message ?: "Order creation failed")
            }
        } catch (e: Exception) {
            Napier.e("OrderRepository: Exception during order creation: ${e.message}", e)
            Result.Error("Order creation failed: ${e.message}")
        }
    }

    override suspend fun getOrder(orderId: String): Result<Order> {
        return try {
            Napier.d("OrderRepository: Fetching order $orderId")
            val response = apiService.getOrder(orderId)
            
            // Check if backend returned Success but with empty data, or returned the object directly
            if (response.success && response.data != null) {
                Result.Success(response.data)
            } else if (response.data != null) { 
                // Some backends might return the object directly without 'success: true' wrapped
                Result.Success(response.data)
            } else {
                Napier.e("OrderRepository: Order fetch failed for $orderId. Error: ${response.error}")
                Result.Error(response.error ?: "Order not found")
            }
        } catch (e: Exception) {
            Napier.e("OrderRepository: Exception fetching order $orderId: ${e.message}")
            Result.Error("Failed to load order: ${e.message}")
        }
    }

    override suspend fun getUserOrders(userId: String): Result<List<Order>> {
        return try {
            val response = apiService.getUserOrders(userId)
            if (response.success && response.data != null) {
                Result.Success(response.data)
            } else {
                Result.Error(response.error ?: "Failed to load orders")
            }
        } catch (e: Exception) {
            Result.Error("Failed to load orders: ${e.message}")
        }
    }

    override suspend fun cancelOrder(orderId: String): Result<Order> {
        return try {
            val response = apiService.updateOrderStatus(orderId, OrderStatus.CANCELLED)
            if (response.success && response.data != null) {
                Result.Success(response.data)
            } else {
                Result.Error(response.error ?: "Failed to cancel order")
            }
        } catch (e: Exception) {
            Result.Error("Cancellation failed: ${e.message}")
        }
    }

    override suspend fun trackOrder(orderId: String): Flow<Order> {
        return socketService.listenForOrderUpdates(orderId)
    }

    override suspend fun updateOrderStatus(orderId: String, status: OrderStatus): Result<Order> {
        return try {
            val response = apiService.updateOrderStatus(orderId, status)
            if (response.success && response.data != null) {
                Result.Success(response.data)
            } else {
                Result.Error(response.error ?: "Status update failed")
            }
        } catch (e: Exception) {
            Result.Error("Status update failed: ${e.message}")
        }
    }

}
