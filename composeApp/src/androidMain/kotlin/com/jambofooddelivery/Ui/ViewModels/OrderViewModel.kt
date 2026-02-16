package com.jambofooddelivery.Ui.ViewModels

import com.jambofooddelivery.domain.TrackOrderUseCase
import com.jambofooddelivery.models.Location
import com.jambofooddelivery.models.Order
import com.jambofooddelivery.models.OrderStatus
import com.jambofooddelivery.remote.SocketService
import com.jambofooddelivery.repositories.OrderRepository
import com.jambofooddelivery.utils.Result
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
                setState { it.copy(riderLocation = location) }
                emitEvent(OrderTrackingEvent.RiderLocationUpdated(location))
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

        // Check the actual tracking history from the backend for each status
        val historyStatusSet = order.tracking.map { it.status }.toSet()

        return statuses.map { (status, message) ->
            val historyEntry = order.tracking.find { it.status == status }
            TrackingHistoryItem(
                timestamp = historyEntry?.createdAt?.toEpochMilliseconds() ?: System.currentTimeMillis(),
                status = status,
                message = message,
                // A step is completed if it's in the tracking history OR if the current order status is further ahead
                isCompleted = historyStatusSet.contains(status) || order.status.ordinal >= status.ordinal
            )
        }
    }

    override fun onCleared() {
        orderTrackingJob?.cancel()
        super.onCleared()
    }
}
