package com.example.jambofooddelivery


import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.jambofooddelivery.cache.AndroidDatabaseDriverFactory
import com.example.jambofooddelivery.cache.Database
import com.example.jambofooddelivery.cache.DatabaseDriverFactory
import com.example.jambofooddelivery.ui.ViewModels.AuthViewModel
import com.example.jambofooddelivery.ui.ViewModels.CartViewModel
import com.example.jambofooddelivery.ui.ViewModels.ChatViewModel
import com.example.jambofooddelivery.ui.ViewModels.CheckoutViewModel
import com.example.jambofooddelivery.ui.ViewModels.HomeViewModel
import com.example.jambofooddelivery.ui.ViewModels.NotificationViewModel
import com.example.jambofooddelivery.ui.ViewModels.OrderTrackingViewModel
import com.example.jambofooddelivery.ui.ViewModels.ProfileViewModel
import com.example.jambofooddelivery.ui.ViewModels.RestaurantViewModel
import com.example.jambofooddelivery.utils.NotificationHelper
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module


//val androidModule = module {
//    // Platform-specific Android dependencies
//    single<Context> { androidContext() }
//
////    single { DatabaseDriverFactory(get()) }
//    single {
//        val driverFactory = AndroidDatabaseDriverFactory(get())
//        Database(driverFactory)
//    }
//    // Include ViewModel module
//    includes(viewModelModule)
//}
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



//    viewModel { RestaurantViewModel() }
//    viewModel { AuthViewModel() }
//    viewModel { CartViewModel() }
//    viewModel { ChatViewModel() }
//    viewModel { CheckoutViewModel() }
//    viewModel { HomeViewModel() }
//    viewModel { OrderTrackingViewModel() }
//    viewModel { ProfileViewModel() }



    single<DatabaseDriverFactory> { AndroidDatabaseDriverFactory(get()) }
    
    // Notification Helper
    single { NotificationHelper(get()) }

}




//package com.example.jambofooddelivery
//
//import com.example.jambofooddelivery.cache.AndroidDatabaseDriverFactory
//import com.example.jambofooddelivery.cache.DatabaseDriverFactory
//import com.example.jambofooddelivery.ui.ViewModels.AuthViewModel
//import com.example.jambofooddelivery.ui.ViewModels.CartViewModel
//import com.example.jambofooddelivery.ui.ViewModels.ChatViewModel
//import com.example.jambofooddelivery.ui.ViewModels.CheckoutViewModel
//import com.example.jambofooddelivery.ui.ViewModels.HomeViewModel
//import com.example.jambofooddelivery.ui.ViewModels.OrderTrackingViewModel
//import com.example.jambofooddelivery.ui.ViewModels.ProfileViewModel
//import com.example.jambofooddelivery.ui.ViewModels.RestaurantViewModel
//import com.russhwolf.settings.Settings
//import com.russhwolf.settings.SharedPreferencesSettings
//import org.koin.androidx.viewmodel.dsl.viewModel
//import org.koin.dsl.module
//
//val androidModule = module {
//
//    // Provide Android-specific implementations for platform interfaces
//    single<DatabaseDriverFactory> { AndroidDatabaseDriverFactory(get()) }
////    single<Settings> { SharedPreferencesSettings(get()) }
//
//    // Define all your ViewModels.
//    // Koin will automatically 'get()' the required dependencies (UseCases, Repositories)
//    // from your other modules (like appModule).
//    viewModel { AuthViewModel(get(), get()) }
//    viewModel { HomeViewModel(get(), get(), get()) }
//    viewModel { ProfileViewModel(get(), get(), get()) }
//    viewModel { RestaurantViewModel(get()) }
//    viewModel { CartViewModel() }
//    viewModel { CheckoutViewModel(get(), get(), get(), get()) }
//    viewModel { OrderTrackingViewModel(get(), get(), get()) }
//    viewModel { ChatViewModel(get(), get(), get()) }
//}
