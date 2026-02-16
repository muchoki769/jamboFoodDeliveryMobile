package com.jambofooddelivery.Ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jambofooddelivery.repositories.AuthRepository
import kotlinx.coroutines.delay
import org.koin.compose.koinInject

@Composable
fun SplashScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToHome: () -> Unit
) {
    val authRepository: AuthRepository = koinInject()
    // Using a state that represents if we've checked the auth status
    val userState by authRepository.getCurrentUser().collectAsState(initial = null)
    
    // Wrap navigation callbacks to ensure we use the latest ones
    val currentOnNavigateToLogin by rememberUpdatedState(onNavigateToLogin)
    val currentOnNavigateToHome by rememberUpdatedState(onNavigateToHome)

    LaunchedEffect(userState) {
        // We wait for the first non-null emission or a short delay if we want to ensure splash visibility
        // If your AuthRepository emits a 'null' initially while loading, we might need a separate 'isLoading' flag
        // But typically, if getCurrentUser() is a Flow from a DB/Settings, it might emit null for "no user"
        
        // Wait at least 1.5 seconds for branding
        delay(1500)
        
        // This logic assumes that 'userState' being determined (even if null) means loading is finished
        // If your flow starts with null and stays null when not logged in, we navigate.
        if (userState != null) {
            currentOnNavigateToHome()
        } else {
            // Note: If you have a specific "loading" state in your Flow, check for it here.
            // If the repo is still loading, this might trigger prematurely.
            // Assuming for now that null = not logged in after the delay.
            currentOnNavigateToLogin()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Restaurant,
                contentDescription = "App Logo",
                modifier = Modifier.size(120.dp),
                tint = Color.White
            )

            Text(
                text = "Jambo Food Delivery",
                style = MaterialTheme.typography.displaySmall,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Delicious food delivered to your doorstep",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )

            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = Color.White,
                strokeWidth = 3.dp
            )
        }
    }
}
