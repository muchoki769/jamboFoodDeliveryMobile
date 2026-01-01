package com.example.jambofooddelivery.repositories

import com.example.jambofooddelivery.models.Address
import com.example.jambofooddelivery.models.Order
import com.example.jambofooddelivery.models.OrderStatus
import com.example.jambofooddelivery.models.PaymentMethod
import com.example.jambofooddelivery.preferences.AppSettings
import com.example.jambofooddelivery.remote.ApiService
import com.example.jambofooddelivery.remote.CreateOrderRequest
import com.example.jambofooddelivery.remote.OrderItemRequest
import com.example.jambofooddelivery.remote.OrderResponse
import com.example.jambofooddelivery.remote.SocketService
import kotlinx.coroutines.flow.Flow
import com.example.jambofooddelivery.utils.Result

interface OrderRepository {
    suspend fun createOrder(
        restaurantId: String,
        items: List<OrderItemRequest>,
        deliveryAddress: Address,
        paymentMethod: PaymentMethod,
        specialInstructions: String? = null,
        mpesaPhone: String? = null
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
        specialInstructions: String?,
        mpesaPhone: String?
    ): Result<OrderResponse> {
        return try {
            val request = CreateOrderRequest(
                restaurantId = restaurantId,
                items = items,
                deliveryAddress = deliveryAddress,
                paymentMethod = paymentMethod,
                specialInstructions = specialInstructions,
                mpesaPhone = mpesaPhone
            )

            val response = apiService.createOrder(request)
            if (response.success && response.data != null) {
                Result.Success(response.data)
            } else {
                Result.Error(response.error ?: "Order creation failed")
            }
        } catch (e: Exception) {
            Result.Error("Order creation failed: ${e.message}")
        }
    }

    override suspend fun getOrder(orderId: String): Result<Order> {
        return try {
            val response = apiService.getOrder(orderId)
            if (response.success && response.data != null) {
                Result.Success(response.data)
            } else {
                Result.Error(response.error ?: "Order not found")
            }
        } catch (e: Exception) {
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