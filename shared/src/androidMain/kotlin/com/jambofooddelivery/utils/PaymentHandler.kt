package com.jambofooddelivery.utils

import android.app.Activity
import co.paystack.android.Paystack
import co.paystack.android.PaystackSdk
import co.paystack.android.Transaction
import co.paystack.android.model.Charge
import com.jambofooddelivery.models.Order
import com.jambofooddelivery.preferences.AppSettings
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class AndroidPaymentHandler(
    private val activity: Activity,
    private val appSettings: AppSettings
) : PaymentHandler {
    override fun openPaystackPayment(
        order: Order,
        accessCode: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val currentUser = appSettings.getCurrentUser()
        val userEmail = currentUser?.email ?: "customer@jambofood.com" // Fallback email

        val charge = Charge()
        charge.amount = (order.finalAmountDouble * 100).toInt()
        charge.email = userEmail
        charge.currency = "KES"
        charge.accessCode = accessCode

        PaystackSdk.chargeCard(activity, charge, object : Paystack.TransactionCallback {
            override fun onSuccess(transaction: Transaction) {
                onSuccess()
            }

            override fun beforeValidate(transaction: Transaction) {
                // Optional: handle pre-validation logic
            }

            override fun onError(error: Throwable, transaction: Transaction?) {
                onError(error.message ?: "Unknown error")
            }
        })
    }
}

/**
 * Actual implementation for Android. 
 * Uses Koin to retrieve the platform-specific implementation.
 */
actual fun getPaymentHandler(): PaymentHandler = object : KoinComponent {
    val handler: PaymentHandler by inject<PaymentHandler>()
}.handler
