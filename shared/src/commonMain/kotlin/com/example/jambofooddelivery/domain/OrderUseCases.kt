package com.example.jambofooddelivery.domain

import com.example.jambofooddelivery.models.Address
import com.example.jambofooddelivery.models.Order
import com.example.jambofooddelivery.models.PaymentMethod
import com.example.jambofooddelivery.remote.OrderItemRequest
import com.example.jambofooddelivery.remote.OrderResponse
import com.example.jambofooddelivery.repositories.OrderRepository
import com.example.jambofooddelivery.utils.Result
import kotlinx.coroutines.flow.Flow

class CreateOrderUseCase(
    private val orderRepository: OrderRepository
) {
    suspend operator fun invoke(
        restaurantId: String,
        items: List<OrderItemRequest>,
        deliveryAddress: Address,
        paymentMethod: PaymentMethod,
        specialInstructions: String? = null,
        mpesaPhone: String? = null
    ): Result<OrderResponse> {
        return orderRepository.createOrder(
            restaurantId,
            items,
            deliveryAddress,
            paymentMethod,
            specialInstructions,
            mpesaPhone
        )
    }
}

class TrackOrderUseCase(
    private val orderRepository: OrderRepository
) {
    suspend operator fun invoke(orderId: String): Flow<Order> {
        return orderRepository.trackOrder(orderId)
    }
}