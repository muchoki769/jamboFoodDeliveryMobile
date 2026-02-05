package com.jambofooddelivery.repositories

import com.jambofooddelivery.models.PaymentMethod
import com.jambofooddelivery.remote.ApiService
import com.jambofooddelivery.remote.MpesaStatusResponse
import com.jambofooddelivery.remote.PaymentIntentRequest
import com.jambofooddelivery.remote.PaymentIntentResponse
import com.jambofooddelivery.remote.PaystackInitiateRequest
import com.jambofooddelivery.remote.PaystackInitiateResponse
import com.jambofooddelivery.utils.Result

interface PaymentRepository {
    suspend fun createStripePaymentIntent(orderId: String, customerId: String, amount: Double): Result<PaymentIntentResponse>
    suspend fun initiatePaystackPayment(orderId: String, email: String, amount: Double, currency: String): Result<PaystackInitiateResponse>
    suspend fun verifyPaystackPayment(reference: String): Result<Boolean>
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
                PaymentIntentRequest(
                    orderId = orderId, 
                    customerId = customerId, 
                    amount = amount,
                    currency = "usd"
                )
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

    override suspend fun initiatePaystackPayment(
        orderId: String,
        email: String,
        amount: Double,
        currency: String
    ): Result<PaystackInitiateResponse> {
        return try {
            val response = apiService.initiatePaystackPayment(
                PaystackInitiateRequest(
                    orderId = orderId,
                    customerEmail = email,
                    amount = amount,
                    currency = currency
                )
            )
            if (response.success && response.data != null) {
                Result.Success(response.data)
            } else {
                Result.Error(response.error ?: "Paystack initialization failed")
            }
        } catch (e: Exception) {
            Result.Error("Payment setup failed: ${e.message}")
        }
    }

    override suspend fun verifyPaystackPayment(reference: String): Result<Boolean> {
        return try {
            val response = apiService.verifyPaystackPayment(reference)
            if (response.success && response.data != null) {
                Result.Success(response.data)
            } else {
                Result.Error(response.error ?: "Verification failed")
            }
        } catch (e: Exception) {
            Result.Error("Verification failed: ${e.message}")
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
            PaymentMethod.PAYSTACK,
            PaymentMethod.MPESA,
            PaymentMethod.CASH
        ))
    }
}
