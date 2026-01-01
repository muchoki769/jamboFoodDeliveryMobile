package com.example.jambofooddelivery.di



import org.koin.core.module.Module
import org.koin.dsl.module

//actual val platformAndroidModule: Module = module {
//    // This is where you add Android-specific Koin dependencies.
//     single { AndroidLocationService(get()) }
//}


import android.content.Context
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import org.koin.android.ext.koin.androidContext

actual val platformModule: Module = module {
    single<Settings> {
        val sharedPrefs = androidContext().getSharedPreferences(
            "jambo_food_preferences",
            Context.MODE_PRIVATE
        )
        SharedPreferencesSettings(sharedPrefs)
    }
}




