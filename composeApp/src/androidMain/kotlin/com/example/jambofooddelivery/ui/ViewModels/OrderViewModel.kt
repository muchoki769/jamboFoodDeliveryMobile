package com.example.jambofooddelivery.ui.ViewModels

import com.example.jambofooddelivery.domain.TrackOrderUseCase
import com.example.jambofooddelivery.models.Location
import com.example.jambofooddelivery.models.Order
import com.example.jambofooddelivery.models.OrderStatus
import com.example.jambofooddelivery.remote.SocketService
import com.example.jambofooddelivery.repositories.OrderRepository
import com.example.jambofooddelivery.utils.Result
import kotlinx.coroutines.Job
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

data class OrderTrackingState(
    val isLoading: Boolean = false,
    val order: Order? = null,
    val riderLocation: Location? = null,
    val estimatedDeliveryTime: String = "",
    val etaMinutes: Int? = null,
    val currentStatus: OrderStatus = OrderStatus.PENDING,
    val trackingHistory: List<TrackingHistoryItem> = emptyList(),
    val isRiderNearby: Boolean = false,
    val error: String? = null
)

sealed class OrderTrackingEvent {
    data class StatusUpdated(val newStatus: OrderStatus) : OrderTrackingEvent()
    data class RiderLocationUpdated(val location: Location) : OrderTrackingEvent()
    data class ShowError(val message: String) : OrderTrackingEvent()
    object OrderDelivered : OrderTrackingEvent()
    object RiderNearby : OrderTrackingEvent()
}

data class TrackingHistoryItem(
    val timestamp: Long,
    val status: OrderStatus,
    val message: String,
    val location: Location? = null,
    val isCompleted: Boolean = false
)

class OrderTrackingViewModel : BaseViewModel<OrderTrackingState, OrderTrackingEvent>(OrderTrackingState()), KoinComponent {
    private val trackOrderUseCase: TrackOrderUseCase by inject()
    private val orderRepository: OrderRepository by inject()
    private val socketService: SocketService by inject()

    private var orderTrackingJob: Job? = null

    fun loadOrder(orderId: String) {
        launch {
            setState { it.copy(isLoading = true, error = null) }

            when (val result = orderRepository.getOrder(orderId)) {
                is Result.Success<*> -> {
                    val order = result.data as Order
                    setState {
                        it.copy(
                            isLoading = false,
                            order = order,
                            currentStatus = order.status,
                            trackingHistory = generateTrackingHistory(order)
                        )
                    }
                    startOrderTracking(orderId)
                }
                is Result.Error -> {
                    setState {
                        it.copy(
                            isLoading = false,
                            error = result.message
                        )
                    }
                    emitEvent(OrderTrackingEvent.ShowError(result.message))
                }
                is Result.Loading -> {
                    // Already set via setState
                }
            }
        }
    }

    private fun startOrderTracking(orderId: String) {
        orderTrackingJob = launch {
            // Listen for real-time order updates
            trackOrderUseCase(orderId).collect { order ->
                setState {
                    it.copy(
                        order = order,
                        currentStatus = order.status,
                        trackingHistory = generateTrackingHistory(order)
                    )
                }

                emitEvent(OrderTrackingEvent.StatusUpdated(order.status))

                if (order.status == OrderStatus.DELIVERED) {
                    emitEvent(OrderTrackingEvent.OrderDelivered)
                }
            }
        }

        // Listen for rider location updates
        launch {
            socketService.listenForRiderLocation(orderId).collect { location ->
                // Backend provides ETA and status updates via socket
                // We assume the location object might contain metadata or there's a separate stream
                // For now, update the state with the new location
                setState { it.copy(riderLocation = location) }
                emitEvent(OrderTrackingEvent.RiderLocationUpdated(location))
                
                // If backend provides eta in the location or order object, update it here
            }
        }
    }

    private fun generateTrackingHistory(order: Order): List<TrackingHistoryItem> {
        val statuses = listOf(
            OrderStatus.PENDING to "Order Placed",
            OrderStatus.PAYMENT_SUCCESSFUL to "Payment Confirmed",
            OrderStatus.CONFIRMED to "Order Received",
            OrderStatus.PREPARING to "Food is being prepared",
            OrderStatus.READY to "Order Packed",
            OrderStatus.PICKED_UP to "Rider picked up your order",
            OrderStatus.ON_THE_WAY to "Order on the way",
            OrderStatus.DELIVERED to "Delivered"
        )

        return statuses.map { (status, message) ->
            TrackingHistoryItem(
                timestamp = System.currentTimeMillis(),
                status = status,
                message = message,
                isCompleted = order.status.ordinal >= status.ordinal
            )
        }
    }

    override fun onCleared() {
        orderTrackingJob?.cancel()
        super.onCleared()
    }
}
