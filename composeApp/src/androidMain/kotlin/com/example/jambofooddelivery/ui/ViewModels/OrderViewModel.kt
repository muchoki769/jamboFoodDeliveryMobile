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
    val currentStatus: OrderStatus = OrderStatus.PENDING,
    val trackingHistory: List<TrackingHistoryItem> = emptyList(),
    val error: String? = null
)

sealed class OrderTrackingEvent {
    data class StatusUpdated(val newStatus: OrderStatus) : OrderTrackingEvent()
    data class RiderLocationUpdated(val location: Location) : OrderTrackingEvent()
    data class ShowError(val message: String) : OrderTrackingEvent()
    object OrderDelivered : OrderTrackingEvent()
}

data class TrackingHistoryItem(
    val timestamp: Long,
    val status: OrderStatus,
    val message: String,
    val location: Location? = null
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

    fun cancelOrder(orderId: String) {
        launch {
            setState { it.copy(isLoading = true) }

            when (val result = orderRepository.cancelOrder(orderId)) {
                is Result.Success<*> -> {
                     val order = result.data as Order
                    setState {
                        it.copy(
                            isLoading = false,
                            order = order,
                            currentStatus = OrderStatus.CANCELLED
                        )
                    }
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

    fun refreshOrder(orderId: String) {
        loadOrder(orderId)
    }

    fun clearError() {
        setState { it.copy(error = null) }
    }

//    override fun clear() {
//        orderTrackingJob?.cancel()
//        super.clear()
//    }
    override fun onCleared() {
        orderTrackingJob?.cancel()
        super.onCleared()
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
        val events = mutableListOf<TrackingHistoryItem>()
        val now = System.currentTimeMillis() 

        fun createItem(status: OrderStatus, message: String) = TrackingHistoryItem(
            timestamp = now,
            status = status,
            message = message
        )
        
        if (order.status.ordinal >= OrderStatus.PENDING.ordinal) {
             events.add(createItem(OrderStatus.PENDING, "Order Placed"))
        }
        if (order.status.ordinal >= OrderStatus.CONFIRMED.ordinal) {
             events.add(createItem(OrderStatus.CONFIRMED, "Order Confirmed"))
        }
        if (order.status.ordinal >= OrderStatus.PREPARING.ordinal) {
             events.add(createItem(OrderStatus.PREPARING, "Preparing your food"))
        }
        if (order.status.ordinal >= OrderStatus.READY.ordinal) {
             events.add(createItem(OrderStatus.READY, "Order Ready"))
        }
        if (order.status.ordinal >= OrderStatus.PICKED_UP.ordinal) {
             events.add(createItem(OrderStatus.PICKED_UP, "Rider picked up order"))
        }
        if (order.status.ordinal >= OrderStatus.ON_THE_WAY.ordinal) {
             events.add(createItem(OrderStatus.ON_THE_WAY, "Order on the way"))
        }
        if (order.status.ordinal >= OrderStatus.DELIVERED.ordinal) {
             events.add(createItem(OrderStatus.DELIVERED, "Delivered"))
        }
        if (order.status == OrderStatus.CANCELLED) {
            events.add(createItem(OrderStatus.CANCELLED, "Order Cancelled"))
        }

        return events
    }
}