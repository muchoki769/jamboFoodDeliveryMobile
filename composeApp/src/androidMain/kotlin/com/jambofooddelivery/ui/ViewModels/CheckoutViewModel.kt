package com.jambofooddelivery.ui.ViewModels

import com.jambofooddelivery.domain.CreateOrderUseCase
import com.jambofooddelivery.domain.ProcessPaymentUseCase
import com.jambofooddelivery.models.Address
import com.jambofooddelivery.models.Order
import com.jambofooddelivery.models.PaymentMethod
import com.jambofooddelivery.models.CartItem
import com.jambofooddelivery.models.PaymentStatus
import com.jambofooddelivery.preferences.AppSettings
import com.jambofooddelivery.remote.OrderItemRequest
import com.jambofooddelivery.repositories.CartRepository
import com.jambofooddelivery.repositories.LocationRepository
import com.jambofooddelivery.repositories.PaymentRepository
import com.jambofooddelivery.repositories.RestaurantRepository
import com.jambofooddelivery.utils.Result
import com.jambofooddelivery.utils.NotificationHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject


data class CheckoutState(
    val isLoading: Boolean = false,
    val cartItems: List<CartItem> = emptyList(),
    val restaurantId: String? = null,
    val subtotal: Double = 0.0,
    val tax: Double = 0.0,
    val deliveryFee: Double = 0.0,
    val totalAmount: Double = 0.0,
    val deliveryAddress: Address? = null,
    val paymentMethods: List<PaymentMethod> = emptyList(),
    val selectedPaymentMethod: PaymentMethod? = null,
    val mpesaPhone: String = "",
    val specialInstructions: String = "",
    val isProcessingPayment: Boolean = false,
    val error: String? = null,
    val order: Order? = null,
    val isPaymentSuccessful: Boolean = false,
    val successMessage: String? = null,
    val mpesaCheckoutRequestId: String? = null
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
    private val cartRepository: CartRepository by inject()
    private val restaurantRepository: RestaurantRepository by inject()
    private val appSettings: AppSettings by inject()
    private val notificationHelper: NotificationHelper by inject()

    init {
        loadPaymentMethods()
        loadCachedData()
        observeCart()
    }

    private fun loadCachedData() {
        val cachedLocation = appSettings.getCachedLocation()
        cachedLocation?.let { loc ->
            val address = Address(
                street = loc.address ?: appSettings.getCachedAddress(),
                city = "",
                state = "",
                postalCode = "",
                country = "",
                latitude = loc.latitude,
                longitude = loc.longitude
            )
            setState { it.copy(deliveryAddress = address) }
        }
        loadUserAddress()
    }

    private fun observeCart() {
        launch {
            cartRepository.getCartItems().collectLatest { items ->
                val subtotal = items.sumOf { it.menuItem.price * it.quantity }
                val tax = subtotal * 0.16
                
                val restaurantId = items.firstOrNull()?.restaurantId
                
                var deliveryFee = 0.0
                if (restaurantId != null) {
                    when (val result = restaurantRepository.getRestaurantById(restaurantId)) {
                        is Result.Success -> {
                            deliveryFee = result.data.deliveryFeeDouble
                        }
                        else -> {
                            deliveryFee = 1.0
                        }
                    }
                }
                
                setState {
                    it.copy(
                        cartItems = items,
                        restaurantId = restaurantId,
                        subtotal = subtotal,
                        tax = tax,
                        deliveryFee = deliveryFee,
                        totalAmount = subtotal + tax + deliveryFee
                    )
                }
            }
        }
    }


    fun createOrder() {
        launch {
            setState { it.copy(isLoading = true, error = null) }

            val address = state.value.deliveryAddress
            val paymentMethod = state.value.selectedPaymentMethod
            val cartItems = state.value.cartItems
            val restaurantId = state.value.restaurantId
            val currentUser = appSettings.getCurrentUser()

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

            if (restaurantId == null) {
                setState { it.copy(isLoading = false, error = "Restaurant information missing") }
                return@launch
            }

            if (currentUser == null) {
                 setState { it.copy(isLoading = false, error = "User session expired") }
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
                restaurantId = restaurantId, 
                items = orderItems,
                deliveryAddress = address,
                paymentMethod = paymentMethod,
                specialInstructions = state.value.specialInstructions.ifBlank { "" },
                mpesaPhone = if (paymentMethod == PaymentMethod.MPESA) state.value.mpesaPhone else null
            )) {
                is Result.Success -> {
                    val orderResponse = result.data
                    val orderId = orderResponse.order.id
                    setState {
                        it.copy(
                            isLoading = false,
                            order = orderResponse.order
                        )
                    }
                    emitEvent(CheckoutEvent.OrderCreated(orderId))

                    when (paymentMethod) {
                        PaymentMethod.PAYSTACK -> processPaystackPayment(orderId, currentUser.email, orderResponse.order.finalAmountDouble)
                        PaymentMethod.MPESA -> {
                            if (orderResponse.order.paymentStatus == PaymentStatus.PAID) {
                                notificationHelper.showNotification(
                                    title = "Payment Successful",
                                    message = "M-Pesa payment was successful!",
                                    orderId = orderId
                                )
                                setState { it.copy(isPaymentSuccessful = true) }
                                emitEvent(CheckoutEvent.NavigateToOrderTracking)
                            } else {
                                val checkoutRequestId = orderResponse.payment?.checkoutRequestId
                                if (checkoutRequestId != null) {
                                    processMpesaPaymentFromOrder(orderId, checkoutRequestId)
                                } else {
                                    setState { it.copy(error = "M-Pesa initialization failed") }
                                    emitEvent(CheckoutEvent.ShowError("M-Pesa initialization failed"))
                                }
                            }
                        }
                        PaymentMethod.CASH -> completeCashOrder(orderId)
                        PaymentMethod.STRIPE -> processStripePayment(orderId, currentUser.id, orderResponse.order.finalAmountDouble)
                    }
                    
                    cartRepository.clearCart()
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
                 is Result.Loading -> {}
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

    private fun loadPaymentMethods() {
        launch {
            when (val result = processPaymentUseCase.getPaymentMethods()) {
                is Result.Success -> {
                    setState { it.copy(paymentMethods = result.data) }
                }
                is Result.Error -> {
                    setState {
                        it.copy(
                            paymentMethods = listOf(
                                PaymentMethod.PAYSTACK,
                                PaymentMethod.MPESA,
                                PaymentMethod.CASH
                            )
                        )
                    }
                }
                 is Result.Loading -> {}
            }
        }
    }

    private fun loadUserAddress() {
        launch {
            val location = locationRepository.getCurrentLocation()
            location?.let {
                when (val result = locationRepository.reverseGeocode(it)) {
                    is Result.Success -> {
                        val addressString = result.data
                        val address = Address(
                            street = addressString,
                            city = "",
                            state = "",
                            postalCode = "",
                            country = "",
                            latitude = it.latitude,
                            longitude = it.longitude
                        )
                        setState { it.copy(deliveryAddress = address) }
                        appSettings.saveCachedLocation(it, addressString)
                    }
                    is Result.Error -> {}
                     is Result.Loading -> {}
                }
            }
        }
    }


    private fun processPaystackPayment(orderId: String, email: String, amount: Double) {
        launch {
            setState { it.copy(isProcessingPayment = true) }

            when (val result = processPaymentUseCase.initiatePaystackPayment(orderId, email, amount, "KES")) {
                is Result.Success -> {
                    setState { it.copy(isProcessingPayment = false) }
                    emitEvent(CheckoutEvent.PaymentIntentCreated(result.data.accessCode))
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
                 is Result.Loading -> {}
            }
        }
    }

    private fun processStripePayment(orderId: String, customerId: String, amount: Double) {
        launch {
            setState { it.copy(isProcessingPayment = true) }

            when (val result = processPaymentUseCase.createStripePaymentIntent(orderId, customerId, amount)) {
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
                is Result.Loading -> {}
            }
        }
    }

    private fun processMpesaPaymentFromOrder(orderId: String, checkoutRequestId: String) {
        launch {
            setState { it.copy(
                isProcessingPayment = true,
                mpesaCheckoutRequestId = checkoutRequestId,
                successMessage = "STK Push sent! Please enter your PIN on your phone."
            ) }
            
            emitEvent(CheckoutEvent.MpesaPaymentInitiated(checkoutRequestId))
            
            notificationHelper.showNotification(
                title = "Payment Initiated",
                message = "Please check your phone for the M-Pesa prompt.",
                orderId = orderId
            )

            // Wait for user interaction and backend callback processing
//            delay(10000)

            pollMpesaStatus(checkoutRequestId, orderId)

            // Final check on order status before moving
//            notificationHelper.showNotification(
//                title = "Payment Successful",
//                message = "M-Pesa payment was successful! A receipt has been sent to your email.",
//                orderId = orderId
//            )
//
//            setState { it.copy(isProcessingPayment = false) }
//            emitEvent(CheckoutEvent.NavigateToOrderTracking)

        }
    }

    private fun pollMpesaStatus(checkoutRequestId: String, orderId: String) {
        launch {
            var attempts = 0
            val maxAttempts = 6 // Poll for ~30 seconds
            var isSuccess = false

            while (attempts < maxAttempts) {
                delay(5000) // Poll every 5 seconds
                when (val result = paymentRepository.getMpesaStatus(checkoutRequestId)) {
                    is Result.Success -> {
                        if (result.data.status == "COMPLETED" || result.data.status == "SUCCESS") {
                            isSuccess = true
                            break
                        } else if (result.data.status == "FAILED" || result.data.status == "CANCELLED") {
                            break
                        }
                    }
                    else -> {} 
                }
                attempts++
            }

            setState { it.copy(isProcessingPayment = false) }

            if (isSuccess) {
                notificationHelper.showNotification(
                    title = "Payment Successful",
                    message = "Your M-Pesa payment was successful!",
                    orderId = orderId
                )
                setState { it.copy(isPaymentSuccessful = true) }
            } else {
                notificationHelper.showNotification(
                    title = "Payment Failed",
                    message = "We couldn\u0027t verify your M-Pesa payment. Please try again or contact support.",
                    orderId = orderId
                )
                setState { it.copy(error = "Payment verification failed") }
            }
            
            emitEvent(CheckoutEvent.NavigateToOrderTracking)
        }
    }

    private fun completeCashOrder(orderId: String) {
        setState { it.copy(
            isPaymentSuccessful = true,
            successMessage = "Order Placed Successfully!"
        ) }
        
        notificationHelper.showNotification(
            title = "Order Placed!",
            message = "Your order has been successfully placed. You will receive a confirmation email.",
            orderId = orderId
        )

        emitEvent(CheckoutEvent.NavigateToOrderTracking)
    }
}
