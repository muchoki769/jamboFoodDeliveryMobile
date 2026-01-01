package com.example.jambofooddelivery.domain

import com.example.jambofooddelivery.models.PaymentMethod
import com.example.jambofooddelivery.remote.MpesaPaymentResponse
import com.example.jambofooddelivery.remote.PaymentIntentResponse
import com.example.jambofooddelivery.repositories.PaymentRepository
import com.example.jambofooddelivery.utils.Result

class ProcessPaymentUseCase(
    private val paymentRepository: PaymentRepository
) {
    // This use case might need to handle different payment methods or just be a wrapper.
    // Since CheckoutViewModel calls specific methods for Stripe and Mpesa, 
    // we might want separate use cases or one that exposes the repository methods.
    // For now, I will just expose the repository methods via this class or create separate ones.
    // However, looking at CheckoutViewModel, it injects ProcessPaymentUseCase but assumes it has... what?
    // Actually CheckoutViewModel calls `paymentRepository.createStripePaymentIntent` and `paymentRepository.initiateMpesaPayment` directly in `processStripePayment` and `processMpesaPayment`.
    // But it INJECTS `processPaymentUseCase` and DOESN'T USE IT (based on analysis "Property 'processPaymentUseCase' is never used").
    // It uses `paymentRepository` which is also injected.
    
    // So I can either remove `ProcessPaymentUseCase` from `CheckoutViewModel` or implement logic in it.
    // I'll implement it to wrap the repository calls.

    suspend fun createStripePaymentIntent(orderId: String, amount: Double): Result<PaymentIntentResponse> {
        return paymentRepository.createStripePaymentIntent(orderId, amount)
    }

    suspend fun initiateMpesaPayment(orderId: String, phone: String, amount: Double): Result<MpesaPaymentResponse> {
        return paymentRepository.initiateMpesaPayment(orderId, phone, amount)
    }
    
    suspend fun getPaymentMethods(): Result<List<PaymentMethod>> {
        return paymentRepository.getPaymentMethods()
    }
}