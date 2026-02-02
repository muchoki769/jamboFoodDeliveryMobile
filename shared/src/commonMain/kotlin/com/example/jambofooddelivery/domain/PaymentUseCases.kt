package com.example.jambofooddelivery.domain

import com.example.jambofooddelivery.models.PaymentMethod
import com.example.jambofooddelivery.remote.MpesaStatusResponse
import com.example.jambofooddelivery.remote.PaymentIntentResponse
import com.example.jambofooddelivery.repositories.PaymentRepository
import com.example.jambofooddelivery.utils.Result

class ProcessPaymentUseCase(
    private val paymentRepository: PaymentRepository
) {
    suspend fun createStripePaymentIntent(orderId: String, amount: Double): Result<PaymentIntentResponse> {
        return paymentRepository.createStripePaymentIntent(orderId, amount)
    }

    suspend fun getMpesaStatus(checkoutRequestId: String): Result<MpesaStatusResponse> {
        return paymentRepository.getMpesaStatus(checkoutRequestId)
    }
    
    suspend fun getPaymentMethods(): Result<List<PaymentMethod>> {
        return paymentRepository.getPaymentMethods()
    }
}
