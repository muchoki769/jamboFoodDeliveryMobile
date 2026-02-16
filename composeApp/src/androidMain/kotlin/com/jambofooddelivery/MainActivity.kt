package com.jambofooddelivery

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.jambofooddelivery.Ui.screens.*
import com.jambofooddelivery.Ui.theme.JamboFoodDeliveryTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val orderIdFromNotification = intent.getStringExtra("orderId")

        setContent {
            JamboFoodDeliveryApp(initialOrderId = orderIdFromNotification)
        }
    }
}

@Composable
fun JamboFoodDeliveryApp(initialOrderId: String? = null) {
    JamboFoodDeliveryTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ){
            val navController = rememberNavController()

            // Handle navigation from notification
            LaunchedEffect(initialOrderId) {
                if (initialOrderId != null) {
                    navController.navigate("order-tracking/$initialOrderId")
                }
            }

            NavHost(
                navController = navController,
                startDestination = "splash"
            ) {
                composable("splash") {
                    SplashScreen(
                        onNavigateToLogin = { 
                            navController.navigate("login") {
                                popUpTo("splash") { inclusive = true }
                            }
                        },
                        onNavigateToHome = { 
                            navController.navigate("home") {
                                popUpTo("splash") { inclusive = true }
                            }
                        }
                    )
                }

                composable("login") {
                    LoginScreen(
                        onLoginSuccess = { 
                            navController.navigate("location-permission") {
                                popUpTo("login") { inclusive = true }
                            }
                        },
                        onNavigateToRegister = { navController.navigate("register") },
                        onNavigateToForgotPassword = { navController.navigate("forgot-password") }
                    )
                }

                composable("register") {
                    RegisterScreen(
                        onRegisterSuccess = { 
                            navController.navigate("location-permission") {
                                popUpTo("register") { inclusive = true }
                            }
                        },
                        onNavigateToLogin = { navController.navigate("login") }
                    )
                }

                composable("forgot-password") {
                    ForgotPasswordScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(
                    route = "reset-password/{token}",
                    arguments = listOf(navArgument("token") { type = NavType.StringType }),
                    deepLinks = listOf(navDeepLink {
                        uriPattern = "https://jambofooddeliverybackend-754053186113.europe-west1.run.app/reset-password?token={token}"
                    })
                ) { backStackEntry ->
                    val token = backStackEntry.arguments?.getString("token") ?: ""
                    ResetPasswordScreen(
                        token = token,
                        onNavigateBack = { navController.popBackStack() },
                        onResetSuccess = {
                            navController.navigate("login") {
                                popUpTo("forgot-password") { inclusive = true }
                            }
                        }
                    )
                }

                composable("location-permission") {
                    LocationPermissionScreen(
                        onLocationGranted = { 
                            navController.navigate("home") {
                                popUpTo("location-permission") { inclusive = true }
                            }
                        },
                        onSkip = { 
                            navController.navigate("home") {
                                popUpTo("location-permission") { inclusive = true }
                            }
                        }
                    )
                }

                composable("home") {
                    HomeScreen(
                        onNavigateToRestaurant = { restaurantId ->
                            navController.navigate("restaurant/$restaurantId")
                        },
                        onNavigateToProfile = { navController.navigate("profile") },
                        onNavigateToOrders = { navController.navigate("orders") },
                        onNavigateToCart = { navController.navigate("cart") },
                        onNavigateToNotifications = { navController.navigate("notifications") },
                        onNavigateToSettings = { navController.navigate("settings") }
                    )
                }

                composable("notifications") {
                    NotificationScreen(
                        onBack = { navController.popBackStack() },
                        onNavigateToOrder = { orderId ->
                            navController.navigate("order-tracking/$orderId")
                        }
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
                            // When order is successful, go to tracking and clear checkout/cart from backstack
                            navController.navigate("order-tracking/$orderId") {
                                popUpTo("home") { inclusive = false }
                            }
                        }
                    )
                }

                composable("order-tracking/{orderId}") { backStackEntry ->
                    val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
                    OrderTrackingScreen(
                        orderId = orderId,
                        onBack = { 
                            // If we came from checkout, back should go to orders or home
                            if (!navController.popBackStack()) {
                                navController.navigate("home")
                            }
                        }
                    )
                }

                composable("orders") {
                    OrdersScreen(
                        onBack = { navController.popBackStack() },
                        onTrackOrder = { orderId ->
                            navController.navigate("order-tracking/$orderId")
                        },
                        onNavigateToHome = { navController.navigate("home") },
                        onNavigateToProfile = { navController.navigate("profile") },
                        onNavigateToSettings = { navController.navigate("settings") }
                    )
                }

                composable("profile") {
                    ProfileScreen(
                        onBack = { navController.popBackStack() },
                        onLogout = { 
                            navController.navigate("login") {
                                popUpTo(0) { inclusive = true }
                            }
                        },
                        onNavigateToSettings = { navController.navigate("settings") },
                        onNavigateToHome = { navController.navigate("home") },
                        onNavigateToOrders = { navController.navigate("orders") }
                    )
                }

                composable("settings") {
                    SettingsScreen(
                        onBack = { navController.popBackStack() },
                        onLogout = {
                            navController.navigate("login") {
                                popUpTo(0) { inclusive = true }
                            }
                        },
                        onNavigateToHome = { navController.navigate("home") },
                        onNavigateToOrders = { navController.navigate("orders") },
                        onNavigateToProfile = { navController.navigate("profile") },
                        onNavigateToSupport = { navController.navigate("support-chat") }
                    )
                }

                composable(
                    route = "support-chat?orderId={orderId}",
                    arguments = listOf(navArgument("orderId") { 
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    })
                ) { backStackEntry ->
                    val orderId = backStackEntry.arguments?.getString("orderId")
                    SupportChatScreen(
                        orderId = orderId,
                        onBack = { navController.popBackStack() }
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
