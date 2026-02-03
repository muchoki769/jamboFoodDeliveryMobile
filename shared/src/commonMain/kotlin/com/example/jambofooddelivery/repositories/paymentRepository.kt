package com.example.jambofooddelivery.repositories

import com.example.jambofooddelivery.models.PaymentMethod
import com.example.jambofooddelivery.remote.ApiService
import com.example.jambofooddelivery.remote.MpesaStatusResponse
import com.example.jambofooddelivery.remote.PaymentIntentRequest
import com.example.jambofooddelivery.remote.PaymentIntentResponse
import com.example.jambofooddelivery.utils.Result

interface PaymentRepository {
    suspend fun createStripePaymentIntent(orderId: String, customerId: String, amount: Double): Result<PaymentIntentResponse>
    suspend fun getMpesaStatus(checkoutRequestId: String): Result<MpesaStatusResponse>
    suspend fun confirmPayment(orderId: String, paymentIntentId: String): Result<Boolean>
    suspend fun getPaymentMethods(): Result<List<PaymentMethod>>
}

class PaymentRepositoryImpl(
    private val apiService: ApiService
) : PaymentRepository {

    override suspend fun createStripePaymentIntent(orderId: String, customerId: String, amount: Double): Result<PaymentIntentResponse> {
        return try {
            val response = apiService.createPaymentIntent(
                PaymentIntentRequest(orderId = orderId, customerId = customerId, amount = amount)
            )
            if (response.success && response.data != null) {
                Result.Success(response.data)
            } else {
                Result.Error(response.error ?: "Payment initialization failed")
            }
        } catch (e: Exception) {
            Result.Error("Payment setup failed: ${e.message}")
        }
    }

    override suspend fun getMpesaStatus(checkoutRequestId: String): Result<MpesaStatusResponse> {
        return try {
            val response = apiService.getMpesaStatus(checkoutRequestId)
            if (response.success && response.data != null) {
                Result.Success(response.data)
            } else {
                Result.Error(response.error ?: "Failed to get M-Pesa status")
            }
        } catch (e: Exception) {
            Result.Error("Failed to get M-Pesa status: ${e.message}")
        }
    }

    override suspend fun confirmPayment(orderId: String, paymentIntentId: String): Result<Boolean> {
        return Result.Success(true)
    }

    override suspend fun getPaymentMethods(): Result<List<PaymentMethod>> {
        return Result.Success(listOf(
            PaymentMethod.STRIPE,
            PaymentMethod.MPESA,
            PaymentMethod.CASH
        ))
    }
}
