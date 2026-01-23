package com.example.jambofooddelivery.ui.ViewModels

import com.example.jambofooddelivery.domain.CreateOrderUseCase
import com.example.jambofooddelivery.domain.ProcessPaymentUseCase
import com.example.jambofooddelivery.models.Address
import com.example.jambofooddelivery.models.Order
import com.example.jambofooddelivery.models.PaymentMethod
import com.example.jambofooddelivery.remote.OrderItemRequest
import com.example.jambofooddelivery.repositories.LocationRepository
import com.example.jambofooddelivery.repositories.PaymentRepository
import com.example.jambofooddelivery.utils.Result
import kotlinx.coroutines.delay
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject


data class CheckoutState(
    val isLoading: Boolean = false,
    val cartItems: List<CartItem> = emptyList(),
    val totalAmount: Double = 0.0,
    val deliveryAddress: Address? = null,
    val paymentMethods: List<PaymentMethod> = emptyList(),
    val selectedPaymentMethod: PaymentMethod? = null,
    val mpesaPhone: String = "",
    val specialInstructions: String = "",
    val isProcessingPayment: Boolean = false,
    val error: String? = null,
    val order: Order? = null
)

sealed class CheckoutEvent {
    data class OrderCreated(val orderId: String) : CheckoutEvent()
    data class PaymentIntentCreated(val clientSecret: String) : CheckoutEvent()
    data class MpesaPaymentInitiated(val checkoutRequestId: String) : CheckoutEvent()
    data class ShowError(val message: String) : CheckoutEvent()
    object NavigateToOrderTracking : CheckoutEvent()
}

class CheckoutViewModel : BaseViewModel<CheckoutState, CheckoutEvent>(CheckoutState()),
    KoinComponent {
    private val createOrderUseCase: CreateOrderUseCase by inject()
    private val processPaymentUseCase: ProcessPaymentUseCase by inject()
    private val locationRepository: LocationRepository by inject()
    private val paymentRepository: PaymentRepository by inject()

    init {
        loadPaymentMethods()
        loadUserAddress()
    }


    fun createOrder() {
        launch {
            setState { it.copy(isLoading = true, error = null) }

            val address = state.value.deliveryAddress
            val paymentMethod = state.value.selectedPaymentMethod
            val cartItems = state.value.cartItems

            if (address == null) {
                setState { it.copy(isLoading = false, error = "Please select a delivery address") }
                return@launch
            }

            if (paymentMethod == null) {
                setState { it.copy(isLoading = false, error = "Please select a payment method") }
                return@launch
            }

            if (cartItems.isEmpty()) {
                setState { it.copy(isLoading = false, error = "Cart is empty") }
                return@launch
            }

            val orderItems = cartItems.map { cartItem ->
                OrderItemRequest(
                    menuItemId = cartItem.menuItem.id,
                    quantity = cartItem.quantity,
                    specialInstructions = cartItem.specialInstructions
                )
            }

            when (val result = createOrderUseCase(
                restaurantId = cartItems.first().menuItem.id, // This should be the restaurant ID
                items = orderItems,
                deliveryAddress = address,
                paymentMethod = paymentMethod,
                specialInstructions = state.value.specialInstructions,
                mpesaPhone = if (paymentMethod == PaymentMethod.MPESA) state.value.mpesaPhone else null
            )) {
                is Result.Success -> {
                    val orderResponse = result.data
                    setState {
                        it.copy(
                            isLoading = false,
                            order = orderResponse.order
                        )
                    }
                    emitEvent(CheckoutEvent.OrderCreated(orderResponse.order.id))

                    // Process payment based on method
                    when (paymentMethod) {
                        PaymentMethod.STRIPE -> processStripePayment(orderResponse.order.id, orderResponse.order.finalAmount)
                        PaymentMethod.MPESA -> processMpesaPayment(orderResponse.order.id, state.value.mpesaPhone, orderResponse.order.finalAmount)
                        PaymentMethod.CASH -> completeCashOrder(orderResponse.order.id)
                    }
                }
                is Result.Error -> {
                    setState {
                        it.copy(
                            isLoading = false,
                            error = result.message
                        )
                    }
                    emitEvent(CheckoutEvent.ShowError(result.message))
                }
                 is Result.Loading -> {
                     // Already set via setState
                 }
            }
        }
    }


    fun setDeliveryAddress(address: Address) {
        setState { it.copy(deliveryAddress = address) }
    }

    fun setPaymentMethod(method: PaymentMethod) {
        setState { it.copy(selectedPaymentMethod = method) }
    }

    fun setMpesaPhone(phone: String) {
        setState { it.copy(mpesaPhone = phone) }
    }

    fun setSpecialInstructions(instructions: String) {
        setState { it.copy(specialInstructions = instructions) }
    }

    fun clearError() {
        setState { it.copy(error = null) }
    }

    private fun loadPaymentMethods() {
        launch {
            when (val result = paymentRepository.getPaymentMethods()) {
                is Result.Success -> {
                    setState { it.copy(paymentMethods = result.data) }
                }
                is Result.Error -> {
                    // Use default payment methods
                    setState {
                        it.copy(
                            paymentMethods = listOf(
                                PaymentMethod.STRIPE,
                                PaymentMethod.MPESA,
                                PaymentMethod.CASH
                            )
                        )
                    }
                }
                 is Result.Loading -> {
                     // Already set via setState
                 }
            }
        }
    }

    private fun loadUserAddress() {
        launch {
            // Get current location and reverse geocode to get address
            val location = locationRepository.getCurrentLocation()
            location?.let {
                when (val result = locationRepository.reverseGeocode(it)) {
                    is Result.Success -> {
                        val addressString = result.data
                        val address = Address(
                            street = addressString,
                            city = "Nairobi", // Fallback or parse from addressString
                            state = "Nairobi",
                            postalCode = "00100",
                            country = "Kenya",
                            latitude = it.latitude,
                            longitude = it.longitude
                        )
                        setState { it.copy(deliveryAddress = address) }
                    }
                    is Result.Error -> {
                        // Use default address or let user enter manually
                    }
                     is Result.Loading -> {
                         // Already set via setState
                     }
                }
            }
        }
    }


    private fun processStripePayment(orderId: String, amount: Double) {
        launch {
            setState { it.copy(isProcessingPayment = true) }

            // Using processPaymentUseCase instead of paymentRepository directly
            when (val result = processPaymentUseCase.createStripePaymentIntent(orderId, amount)) {
                is Result.Success -> {
                    setState { it.copy(isProcessingPayment = false) }
                    emitEvent(CheckoutEvent.PaymentIntentCreated(result.data.clientSecret))
                }
                is Result.Error -> {
                    setState {
                        it.copy(
                            isProcessingPayment = false,
                            error = result.message
                        )
                    }
                    emitEvent(CheckoutEvent.ShowError(result.message))
                }
                 is Result.Loading -> {
                     // Already set via setState
                 }
            }
        }
    }

    private fun processMpesaPayment(orderId: String, phone: String, amount: Double) {
        launch {
            setState { it.copy(isProcessingPayment = true) }

            // Using processPaymentUseCase instead of paymentRepository directly
            when (val result = processPaymentUseCase.initiateMpesaPayment(orderId, phone, amount)) {
                is Result.Success -> {
                    setState { it.copy(isProcessingPayment = false) }
                    emitEvent(CheckoutEvent.MpesaPaymentInitiated(result.data.checkoutRequestId))
                    // Wait for MPesa callback and then navigate to tracking
                    delay(5000) // Simulate waiting for payment confirmation
                    emitEvent(CheckoutEvent.NavigateToOrderTracking)
                }
                is Result.Error -> {
                    setState {
                        it.copy(
                            isProcessingPayment = false,
                            error = result.message
                        )
                    }
                    emitEvent(CheckoutEvent.ShowError(result.message))
                }
                 is Result.Loading -> {
                     // Already set via setState
                 }
            }
        }
    }

    private fun completeCashOrder(orderId: String) {
        // For cash payments, just navigate to tracking
        emitEvent(CheckoutEvent.NavigateToOrderTracking)
    }
}
