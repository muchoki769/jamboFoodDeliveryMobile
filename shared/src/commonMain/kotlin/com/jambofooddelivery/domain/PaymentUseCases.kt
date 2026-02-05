package com.jambofooddelivery.domain

import com.jambofooddelivery.models.PaymentMethod
import com.jambofooddelivery.remote.MpesaStatusResponse
import com.jambofooddelivery.remote.PaymentIntentResponse
import com.jambofooddelivery.remote.PaystackInitiateResponse
import com.jambofooddelivery.repositories.PaymentRepository
import com.jambofooddelivery.utils.Result

class ProcessPaymentUseCase(
    private val paymentRepository: PaymentRepository
) {
    suspend fun createStripePaymentIntent(orderId: String, customerId: String, amount: Double): Result<PaymentIntentResponse> {
        return paymentRepository.createStripePaymentIntent(orderId, customerId, amount)
    }

    suspend fun initiatePaystackPayment(orderId: String, email: String, amount: Double, currency: String): Result<PaystackInitiateResponse> {
        return paymentRepository.initiatePaystackPayment(orderId, email, amount, currency)
    }

    suspend fun verifyPaystackPayment(reference: String): Result<Boolean> {
        return paymentRepository.verifyPaystackPayment(reference)
    }

    suspend fun getMpesaStatus(checkoutRequestId: String): Result<MpesaStatusResponse> {
        return paymentRepository.getMpesaStatus(checkoutRequestId)
    }
    
    suspend fun getPaymentMethods(): Result<List<PaymentMethod>> {
        return paymentRepository.getPaymentMethods()
    }
}
