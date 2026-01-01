package com.example.jambofooddelivery.models

import kotlinx.serialization.Serializable

@Serializable
data class PaymentResponse(
    val id: String,
    val status: String,
    val amount: Double,
    val currency: String,
    val clientSecret: String? = null
)