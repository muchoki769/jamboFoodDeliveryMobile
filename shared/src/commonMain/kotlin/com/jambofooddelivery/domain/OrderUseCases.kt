package com.jambofooddelivery.domain

import com.jambofooddelivery.models.Address
import com.jambofooddelivery.models.Order
import com.jambofooddelivery.models.PaymentMethod
import com.jambofooddelivery.remote.OrderItemRequest
import com.jambofooddelivery.remote.OrderResponse
import com.jambofooddelivery.repositories.OrderRepository
import com.jambofooddelivery.utils.Result
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.Flow

class CreateOrderUseCase(
    private val orderRepository: OrderRepository
) {
    suspend operator fun invoke(
        restaurantId: String,
        items: List<OrderItemRequest>,
        deliveryAddress: Address,
        paymentMethod: PaymentMethod,
        mpesaPhone: String? = null,
        specialInstructions: String? = null,
        tip: Double? = null
    ): Result<OrderResponse> {
        Napier.d("CreateOrderUseCase: Initiating order for restaurant $restaurantId")
        Napier.d("CreateOrderUseCase: Items: $items")
        Napier.d("CreateOrderUseCase: Delivery Address: ${deliveryAddress.fullAddress}")
        Napier.d("CreateOrderUseCase: Payment Method: $paymentMethod, M-Pesa Phone: $mpesaPhone")
        
        val result = orderRepository.createOrder(
            restaurantId = restaurantId,
            items = items,
            deliveryAddress = deliveryAddress,
            paymentMethod = paymentMethod,
            mpesaPhone = mpesaPhone,
            specialInstructions = specialInstructions,
            tip = tip
        )

        when (result) {
            is Result.Success -> {
                Napier.d("CreateOrderUseCase: Order created successfully. Order ID: ${result.data.order.id}")
            }
            is Result.Error -> {
                Napier.e("CreateOrderUseCase: Order creation failed. Error: ${result.message}")
            }
            is Result.Loading -> {
                Napier.d("CreateOrderUseCase: Order creation in progress...")
            }
        }
        
        return result
    }
}

class TrackOrderUseCase(
    private val orderRepository: OrderRepository
) {
    suspend operator fun invoke(orderId: String): Flow<Order> {
        return orderRepository.trackOrder(orderId)
    }
}
