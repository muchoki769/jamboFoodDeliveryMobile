package com.example.jambofooddelivery


import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.jambofooddelivery.cache.AndroidDatabaseDriverFactory
import com.example.jambofooddelivery.cache.DatabaseDriverFactory
import com.example.jambofooddelivery.ui.ViewModels.*
import com.example.jambofooddelivery.utils.NotificationHelper
import org.koin.dsl.module


@RequiresApi(Build.VERSION_CODES.O)
val androidModule = module {

    factory { RestaurantViewModel() }
    factory { AuthViewModel() }
    factory { CartViewModel() }
    factory { ChatViewModel() }
    factory { CheckoutViewModel() }
    factory { HomeViewModel() }
    factory { OrderTrackingViewModel() }
    factory { ProfileViewModel() }
    factory { NotificationViewModel() }
    factory {SettingsViewModel()}


    single<DatabaseDriverFactory> { AndroidDatabaseDriverFactory(get()) }

    // Notification Helper
    single { NotificationHelper(get()) }



//    viewModel { RestaurantViewModel() }
//    viewModel { AuthViewModel() }
//    viewModel { CartViewModel() }
//    viewModel { ChatViewModel() }
//    viewModel { CheckoutViewModel() }
//    viewModel { HomeViewModel() }
//    viewModel { OrderTrackingViewModel() }
//    viewModel { ProfileViewModel() }

}
