package com.example.jambofooddelivery.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.jambofooddelivery.R
import com.example.jambofooddelivery.models.ProfileUpdate
import com.example.jambofooddelivery.ui.ViewModels.ProfileEvent
import com.example.jambofooddelivery.ui.ViewModels.ProfileViewModel
import com.example.jambofooddelivery.ui.components.BottomNavigationBar
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToOrders: () -> Unit
) {
    val viewModel: ProfileViewModel = koinViewModel()
    val state by viewModel.state.collectAsState()
    val events by viewModel.events.collectAsState()
    val scrollState = rememberScrollState()

    var firstName by remember(state.user) { mutableStateOf(state.user?.firstName ?: "") }
    var lastName by remember(state.user) { mutableStateOf(state.user?.lastName ?: "") }
    var phone by remember(state.user) { mutableStateOf(state.user?.phone ?: "") }

    LaunchedEffect(events) {
        when (events) {
            is ProfileEvent.LogoutSuccess -> {
                onLogout()
                viewModel.clearEvent()
            }
            is ProfileEvent.ProfileUpdated -> {
                viewModel.clearEvent()
            }
            is ProfileEvent.ShowError -> {
                viewModel.clearEvent()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile Information") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp).padding(end = 16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        TextButton(onClick = {
                            viewModel.updateProfile(
                                ProfileUpdate(
                                    firstName = firstName,
                                    lastName = lastName,
                                    phone = phone
                                )
                            )
                        }) {
                            Text("Save", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            )
        },
        bottomBar = {
            BottomNavigationBar(
                selectedRoute = "profile",
                onHomeClick = onNavigateToHome,
                onOrdersClick = onNavigateToOrders,
                onProfileClick = { /* Already here */ },
                onSettingsClick = onNavigateToSettings
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Picture Section
            Box(contentAlignment = Alignment.BottomEnd) {
                AsyncImage(
                    model = getCloudinaryUrl(state.user?.avatarUrl, width = 300, height = 300),
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape),
                    placeholder = painterResource(R.drawable.placeholder_profile),
                    error = painterResource(R.drawable.placeholder_profile),
                    contentScale = ContentScale.Crop
                )
                IconButton(
                    onClick = { /* Image picker */ },
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit Picture",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            if (state.isUploadingImage) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Information Fields
            ProfileInputField(
                label = "First Name",
                value = firstName,
                onValueChange = { firstName = it }
            )
            ProfileInputField(
                label = "Last Name",
                value = lastName,
                onValueChange = { lastName = it }
            )
            ProfileInputField(
                label = "Email Address",
                value = state.user?.email ?: "",
                onValueChange = { },
                enabled = false
            )
            ProfileInputField(
                label = "Phone Number",
                value = phone,
                onValueChange = { phone = it }
            )

            Spacer(modifier = Modifier.height(24.dp))
            
            // Stats
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    ProfileStat(label = "Orders", value = state.orders.size.toString())
                    ProfileStat(label = "Member Since", value = state.user?.createdAt?.toString()?.split("-")?.firstOrNull() ?: "2023")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun ProfileInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean = true
) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
        Text(text = label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                focusedContainerColor = androidx.compose.ui.graphics.Color.Transparent
            )
        )
    }
}

@Composable
fun ProfileStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(text = label, style = MaterialTheme.typography.labelMedium)
    }
}
