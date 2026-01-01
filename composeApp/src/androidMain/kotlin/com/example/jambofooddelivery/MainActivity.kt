package com.example.jambofooddelivery

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.jambofooddelivery.repositories.AuthRepository
import com.example.jambofooddelivery.ui.screens.CartScreen
import com.example.jambofooddelivery.ui.screens.CheckoutScreen
import com.example.jambofooddelivery.ui.screens.HomeScreen
import com.example.jambofooddelivery.ui.screens.LoginScreen
import com.example.jambofooddelivery.ui.screens.OrderTrackingScreen
import com.example.jambofooddelivery.ui.screens.OrdersScreen
import com.example.jambofooddelivery.ui.screens.ProfileScreen
import com.example.jambofooddelivery.ui.screens.RegisterScreen
import com.example.jambofooddelivery.ui.screens.RestaurantDetailScreen
import com.example.jambofooddelivery.ui.screens.SplashScreen
import com.example.jambofooddelivery.ui.theme.JamboFoodDeliveryTheme
import org.koin.compose.koinInject


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            JamboFoodDeliveryApp()
        }
    }
}

@Composable
fun JamboFoodDeliveryApp() {
    JamboFoodDeliveryTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ){
            val navController = rememberNavController()
            val authRepository: AuthRepository = koinInject()
            val isLoggedIn by authRepository.getCurrentUser().collectAsState(initial = null)

            LaunchedEffect(isLoggedIn) {
                if (isLoggedIn != null) {
                    navController.navigate("home") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            }

            NavHost(
                navController = navController,

                startDestination = "splash"
            ) {
                composable("splash") {
                    SplashScreen(
                        onNavigateToLogin = { navController.navigate("login") },
                        onNavigateToHome = { navController.navigate("home") }
                    )
                }

                composable("login") {
                    LoginScreen(
                        onLoginSuccess = { navController.navigate("home") },
                        onNavigateToRegister = { navController.navigate("register") }
                    )
                }

                composable("register") {
                    RegisterScreen(
                        onRegisterSuccess = { navController.navigate("home") },
                        onNavigateToLogin = { navController.navigate("login") }
                    )
                }

                composable("home") {
                    HomeScreen(
                        onNavigateToRestaurant = { restaurantId ->
                            navController.navigate("restaurant/$restaurantId")
                        },
                        onNavigateToProfile = { navController.navigate("profile") },
                        onNavigateToOrders = { navController.navigate("orders") },
                        onNavigateToCart = { navController.navigate("cart") }
                    )
                }

                composable("restaurant/{restaurantId}") { backStackEntry ->
                    val restaurantId = backStackEntry.arguments?.getString("restaurantId") ?: ""
                    RestaurantDetailScreen(
                        restaurantId = restaurantId,
                        onBack = { navController.popBackStack() },
                        onAddToCart = { navController.navigate("cart") }
                    )
                }

                composable("cart") {
                    CartScreen(
                        onBack = { navController.popBackStack() },
                        onCheckout = { navController.navigate("checkout") }
                    )
                }

                composable("checkout") {
                    CheckoutScreen(
                        onBack = { navController.popBackStack() },
                        onOrderSuccess = { orderId ->
                            navController.navigate("order-tracking/$orderId")
                        }
                    )
                }

                composable("order-tracking/{orderId}") { backStackEntry ->
                    val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
                    OrderTrackingScreen(
                        orderId = orderId,
                        onBack = { navController.popBackStack() }
                    )
                }

                composable("orders") {
                    OrdersScreen(
                        onBack = { navController.popBackStack() },
                        onTrackOrder = { orderId ->
                            navController.navigate("order-tracking/$orderId")
                        }
                    )
                }

                composable("profile") {
                    ProfileScreen(
                        onBack = { navController.popBackStack() },
                        onLogout = { navController.navigate("login") }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun JamboFoodDeliveryAppPreview() {
    JamboFoodDeliveryApp()
}