package com.jambofooddelivery.Ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jambofooddelivery.models.OrderStatus
import com.jambofooddelivery.Ui.ViewModels.OrderTrackingViewModel
import com.jambofooddelivery.Ui.ViewModels.TrackingHistoryItem
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderTrackingScreen(
    orderId: String,
    onBack: () -> Unit,
) {
    val viewModel: OrderTrackingViewModel = koinViewModel()
    val state by viewModel.state.collectAsState()

    LaunchedEffect(orderId) {
        viewModel.loadOrder(orderId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        if (state.order?.orderNumber != null) "Track Order #${state.order?.orderNumber}" 
                        else "Track Order"
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (state.isLoading && state.order == null) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (state.error != null) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(text = state.error ?: "An error occurred", color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { viewModel.loadOrder(orderId) }) {
                        Text("Retry")
                    }
                }
            } else if (state.order != null) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    item {
                        ETAHeader(etaMinutes = state.etaMinutes, isRiderNearby = state.isRiderNearby)
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    itemsIndexed(state.trackingHistory) { index, item ->
                        TrackingStep(
                            item = item,
                            isLast = index == state.trackingHistory.size - 1,
                            currentStatus = state.currentStatus
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ETAHeader(etaMinutes: Int?, isRiderNearby: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isRiderNearby) "Rider is nearby!" else "Estimated Arrival",
                    style = MaterialTheme.typography.labelLarge
                )
                Text(
                    text = if (isRiderNearby) "Get ready to receive your order" 
                          else if (etaMinutes != null) "$etaMinutes mins" 
                          else "Calculating...",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            if (isRiderNearby) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun TrackingStep(item: TrackingHistoryItem, isLast: Boolean, currentStatus: OrderStatus) {
    val isCompleted = item.isCompleted
    val isCurrent = item.status == currentStatus
    
    val circleColor by animateColorAsState(
        targetValue = if (isCompleted) MaterialTheme.colorScheme.primary 
                     else if (isCurrent) MaterialTheme.colorScheme.secondary
                     else MaterialTheme.colorScheme.surfaceVariant,
        animationSpec = tween(500)
    )
    
    val circleSize by animateDpAsState(
        targetValue = if (isCurrent) 32.dp else 24.dp,
        animationSpec = tween(500)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        verticalAlignment = Alignment.Top
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(circleSize)
                    .clip(CircleShape)
                    .background(circleColor),
                contentAlignment = Alignment.Center
            ) {
                if (isCompleted && !isCurrent) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                } else if (isCurrent) {
                     CircularProgressIndicator(
                         modifier = Modifier.size(20.dp),
                         strokeWidth = 2.dp,
                         color = MaterialTheme.colorScheme.onSecondary
                     )
                }
            }
            
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .fillMaxHeight()
                        .padding(vertical = 4.dp)
                        .background(
                            if (isCompleted) MaterialTheme.colorScheme.primary 
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.padding(bottom = 24.dp)) {
            Text(
                text = item.message,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isCurrent || isCompleted) FontWeight.Bold else FontWeight.Normal,
                color = if (isCurrent || isCompleted) MaterialTheme.colorScheme.onSurface 
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            if (isCurrent) {
                Text(
                    text = "In Progress...",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            } else if (isCompleted) {
                Text(
                    text = "Completed",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
