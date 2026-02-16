package com.jambofooddelivery.Ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import co.paystack.android.PaystackSdk
import com.jambofooddelivery.models.Address
import com.jambofooddelivery.models.PaymentMethod
import com.jambofooddelivery.Ui.ViewModels.CheckoutEvent
import com.jambofooddelivery.Ui.ViewModels.CheckoutViewModel
import com.jambofooddelivery.utils.getPaymentHandler
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    onBack: () -> Unit,
    onOrderSuccess: (String) -> Unit
) {
    val viewModel: CheckoutViewModel = koinViewModel()
    val state by viewModel.state.collectAsState()
    val events by viewModel.events.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Card State
    var cardNumber by remember { mutableStateOf("") }
    var expiryDate by remember { mutableStateOf("") } // MM/YY
    var cvv by remember { mutableStateOf("") }

    // Initialize Paystack once
    LaunchedEffect(Unit) {
        try {
            PaystackSdk.initialize(context.applicationContext)
            PaystackSdk.setPublicKey("pk_test_your_public_key")
        } catch (e: Exception) {
            // SDK might already be initialized
        }
    }

    LaunchedEffect(events) {
        when (val event = events) {
            is CheckoutEvent.NavigateToOrderTracking -> {
                state.order?.id?.let { onOrderSuccess(it) }
                viewModel.clearEvent()
            }
            is CheckoutEvent.OrderCreated -> {
                if (state.selectedPaymentMethod == PaymentMethod.CASH) {
                    onOrderSuccess(event.orderId)
                    viewModel.clearEvent()
                }
            }
            is CheckoutEvent.ShowError -> {
                snackbarHostState.showSnackbar(event.message)
                viewModel.clearEvent()
            }
            is CheckoutEvent.PaymentIntentCreated -> {
                val order = state.order
                if (order != null) {
                    // Use the centralized bridge to handle the payment
                    getPaymentHandler().openPaystackPayment(
                        order = order,
                        accessCode = event.clientSecret,
                        onSuccess = {
                            onOrderSuccess(order.id)
                        },
                        onError = { error ->
                            scope.launch { snackbarHostState.showSnackbar("Payment Error: $error") }
                        }
                    )
                }
                viewModel.clearEvent()
            }
            is CheckoutEvent.MpesaPaymentInitiated -> {
                snackbarHostState.showSnackbar("M-Pesa payment initiated. Please check your phone.")
                viewModel.clearEvent()
            }
            else -> {}
        }
    }

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
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                    enabled = !state.isLoading && !state.isProcessingPayment && state.selectedPaymentMethod != null && state.deliveryAddress != null
                ) {
                    if (state.isLoading || state.isProcessingPayment) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                    } else {
                        Text("Place Order", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                item {
                    SectionHeader("Delivery Address")
                    DeliveryAddressCard(address = state.deliveryAddress, onClick = { /* Address selection */ })
                }

                item {
                    SectionHeader("Payment Method")
                    PaymentMethodSection(
                        methods = state.paymentMethods,
                        selectedMethod = state.selectedPaymentMethod,
                        onMethodSelected = { viewModel.setPaymentMethod(it) },
                        mpesaPhone = state.mpesaPhone,
                        onMpesaPhoneChange = { viewModel.setMpesaPhone(it) },
                        cardNumber = cardNumber,
                        onCardNumberChange = { if (it.length <= 16) cardNumber = it },
                        expiryDate = expiryDate,
                        onExpiryDateChange = { if (it.length <= 5) expiryDate = it },
                        cvv = cvv,
                        onCvvChange = { if (it.length <= 4) cvv = it }
                    )
                }

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
                    OrderSummaryCard(total = state.totalAmount)
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
            
            if (state.isProcessingPayment) {
                Surface(modifier = Modifier.fillMaxSize(), color = Color.Black.copy(alpha = 0.3f)) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Processing Payment...", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
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
    onMpesaPhoneChange: (String) -> Unit,
    cardNumber: String,
    onCardNumberChange: (String) -> Unit,
    expiryDate: String,
    onExpiryDateChange: (String) -> Unit,
    cvv: String,
    onCvvChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        methods.forEach { method ->
            val isSelected = method == selectedMethod
            
            Column {
                Surface(
                    modifier = Modifier.fillMaxWidth().selectable(selected = isSelected, onClick = { onMethodSelected(method) }),
                    shape = MaterialTheme.shapes.medium,
                    tonalElevation = if (isSelected) 4.dp else 0.dp,
                    border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface
                ) {
                    Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = if (isSelected) Icons.Filled.CheckCircle else Icons.Outlined.RadioButtonUnchecked, contentDescription = null, tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(text = when(method) {
                            PaymentMethod.PAYSTACK -> "Global Credit / Debit Card"
                            PaymentMethod.STRIPE -> "Global Credit / Debit Card (Legacy)"
                            PaymentMethod.MPESA -> "M-Pesa"
                            PaymentMethod.CASH -> "Cash on Delivery"
                        }, style = MaterialTheme.typography.bodyLarge, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, modifier = Modifier.weight(1f))
                    }
                }
                
                if ((method == PaymentMethod.PAYSTACK || method == PaymentMethod.STRIPE) && isSelected) {
                    AnimatedVisibility(visible = true) {
                        Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = cardNumber,
                                onValueChange = onCardNumberChange,
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Card Number") },
                                leadingIcon = { Icon(Icons.Default.CreditCard, contentDescription = null) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = MaterialTheme.shapes.medium
                            )
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = expiryDate,
                                    onValueChange = onExpiryDateChange,
                                    modifier = Modifier.weight(1f),
                                    label = { Text("Expiry (MM/YY)") },
                                    placeholder = { Text("MM/YY") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    shape = MaterialTheme.shapes.medium
                                )
                                OutlinedTextField(
                                    value = cvv,
                                    onValueChange = onCvvChange,
                                    modifier = Modifier.weight(1f),
                                    label = { Text("CVV") },
                                    visualTransformation = PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    shape = MaterialTheme.shapes.medium
                                )
                            }
                        }
                    }
                }

                if (method == PaymentMethod.MPESA && isSelected) {
                    AnimatedVisibility(visible = true) {
                        OutlinedTextField(
                            value = mpesaPhone,
                            onValueChange = onMpesaPhoneChange,
                            modifier = Modifier.fillMaxWidth().padding(8.dp),
                            label = { Text("M-Pesa Phone Number") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            shape = MaterialTheme.shapes.medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
}

@Composable
fun DeliveryAddressCard(address: Address?, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick), shape = MaterialTheme.shapes.medium, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                if (address != null) {
                    Text(text = address.street ?: "No street", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                    Text(text = "${address.city ?: ""}, ${address.country ?: ""}", style = MaterialTheme.typography.bodyMedium)
                } else {
                    Text(text = "Loading address...", style = MaterialTheme.typography.bodyMedium)
                }
            }
            TextButton(onClick = onClick) { Text("Change") }
        }
    }
}

@Composable
fun OrderSummaryCard(total: Double) {
    Card(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Estimated Total", style = MaterialTheme.typography.bodyLarge)
                Text("KSh ${"%.0f".format(total)}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
