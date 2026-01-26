package com.example.jambofooddelivery.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.jambofooddelivery.models.Address
import com.example.jambofooddelivery.models.PaymentMethod
import com.example.jambofooddelivery.ui.ViewModels.CheckoutViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    onBack: () -> Unit,
    onOrderSuccess: (String) -> Unit
) {
    val viewModel: CheckoutViewModel = koinViewModel()
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Checkout") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.navigationBarsPadding()
            ) {
                Button(
                    onClick = { viewModel.createOrder() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = MaterialTheme.shapes.medium,
                    contentPadding = PaddingValues(16.dp),
                    enabled = !state.isLoading && state.selectedPaymentMethod != null && state.deliveryAddress != null
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                    } else {
                        Text("Place Order", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Delivery Address Section
            item {
                SectionHeader("Delivery Address")
                DeliveryAddressCard(
                    address = state.deliveryAddress,
                    onClick = { /* Implement address selection/edit */ }
                )
            }

            // Payment Method Section
            item {
                SectionHeader("Payment Method")
                PaymentMethodSection(
                    methods = state.paymentMethods,
                    selectedMethod = state.selectedPaymentMethod,
                    onMethodSelected = { viewModel.setPaymentMethod(it) },
                    mpesaPhone = state.mpesaPhone,
                    onMpesaPhoneChange = { viewModel.setMpesaPhone(it) }
                )
            }

            // Special Instructions
            item {
                SectionHeader("Special Instructions")
                OutlinedTextField(
                    value = state.specialInstructions,
                    onValueChange = { viewModel.setSpecialInstructions(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("E.g. No onions, gate code is 1234...") },
                    minLines = 3,
                    shape = MaterialTheme.shapes.medium
                )
            }

            // Order Summary Mini
            item {
                SectionHeader("Order Summary")
                OrderSummaryCard(
                    total = state.totalAmount // You might want to pass more details here
                )
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }

        // Show error snackbar if any
        state.error?.let { error ->
            LaunchedEffect(error) {
                // You can show a snackbar here
                viewModel.clearError()
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
fun DeliveryAddressCard(address: Address?, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.LocationOn,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                if (address != null) {
                    Text(text = address.street ?: "No street", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                    Text(text = "${address.city ?: ""}, ${address.country ?: ""}", style = MaterialTheme.typography.bodyMedium)
                } else {
                    Text(text = "Loading address...", style = MaterialTheme.typography.bodyMedium)
                }
            }
            TextButton(onClick = onClick) {
                Text("Change")
            }
        }
    }
}

@Composable
fun PaymentMethodSection(
    methods: List<PaymentMethod>,
    selectedMethod: PaymentMethod?,
    onMethodSelected: (PaymentMethod) -> Unit,
    mpesaPhone: String,
    onMpesaPhoneChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        methods.forEach { method ->
            val isSelected = method == selectedMethod
            
            Column {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = isSelected,
                            onClick = { onMethodSelected(method) }
                        ),
                    shape = MaterialTheme.shapes.medium,
                    tonalElevation = if (isSelected) 4.dp else 0.dp,
                    border = BorderStroke(
                        1.dp, 
                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    ),
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isSelected) Icons.Filled.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                            contentDescription = null,
                            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = when(method) {
                                PaymentMethod.STRIPE -> "Credit/Debit Card (Stripe)"
                                PaymentMethod.MPESA -> "M-Pesa"
                                PaymentMethod.CASH -> "Cash on Delivery"
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                
                // Show M-Pesa phone input if selected
                if (method == PaymentMethod.MPESA && isSelected) {
                    AnimatedVisibility(visible = true) {
                        OutlinedTextField(
                            value = mpesaPhone,
                            onValueChange = onMpesaPhoneChange,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp, start = 8.dp, end = 8.dp),
                            label = { Text("M-Pesa Phone Number") },
                            placeholder = { Text("0712345678") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            shape = MaterialTheme.shapes.medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OrderSummaryCard(total: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Estimated Total", style = MaterialTheme.typography.bodyLarge)
                Text(
                    "KSh ${"%.0f".format(total)}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                "Final total including taxes and delivery will be calculated on order confirmation.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
