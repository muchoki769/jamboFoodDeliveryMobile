package com.example.jambofooddelivery.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class PaymentResponse(
    val id: String? = null,
    val status: String? = null,
    val amount: Double? = null,
    val currency: String? = null,
    @SerialName("client_secret")
    val clientSecret: String? = null,
    
    // M-Pesa specific fields from backend response
    val checkoutRequestId: String? = null,
    val customerMessage: String? = null
)
