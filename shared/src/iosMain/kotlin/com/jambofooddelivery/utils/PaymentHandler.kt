package com.jambofooddelivery.utils

import com.jambofooddelivery.models.Order
import platform.Foundation.NSLog

/**
 * iOS implementation of the PaymentHandler.
 * This class acts as a bridge. Swift code should set the [delegate] 
 * to handle the actual Paystack UI presentation.
 */
class IOSPaymentHandler : PaymentHandler {
    
    interface Delegate {
        fun onPaymentRequest(
            orderId: String,
            email: String,
            amount: Double,
            accessCode: String
        )
    }
    
    var delegate: Delegate? = null
    private var lastOnSuccess: (() -> Unit)? = null
    private var lastOnError: ((String) -> Unit)? = null

    override fun openPaystackPayment(
        order: Order,
        accessCode: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        this.lastOnSuccess = onSuccess
        this.lastOnError = onError
        
        NSLog("iOS Bridge: Payment requested for order ${order.id}")
        
        delegate?.onPaymentRequest(
            orderId = order.id,
            email = order.customerId, // Assuming customerId is the email or unique identifier
            amount = order.finalAmountDouble,
            accessCode = accessCode
        ) ?: run {
            NSLog("iOS Bridge ERROR: No delegate set for IOSPaymentHandler")
            onError("Payment system not initialized")
        }
    }

    /**
     * Called by Swift code when Paystack reports success
     */
    fun onPaymentSuccess() {
        lastOnSuccess?.invoke()
    }

    /**
     * Called by Swift code when Paystack reports an error
     */
    fun onPaymentError(error: String) {
        lastOnError?.invoke(error)
    }
}

// Global instance to be accessed from Swift
val iosPaymentHandler = IOSPaymentHandler()

actual fun getPaymentHandler(): PaymentHandler = iosPaymentHandler
