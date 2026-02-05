package com.jambofooddelivery.utils

import com.jambofooddelivery.models.Order

interface PaymentHandler {
    fun openPaystackPayment(
        order: Order,
        accessCode: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    )
}

expect fun getPaymentHandler(): PaymentHandler
