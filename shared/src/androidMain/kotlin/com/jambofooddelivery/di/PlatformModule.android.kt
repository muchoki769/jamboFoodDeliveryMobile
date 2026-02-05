package com.jambofooddelivery.di

import android.content.Context
import com.jambofooddelivery.AndroidLocationService
import com.jambofooddelivery.repositories.PlatformLocationService
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single<Settings> {
        val sharedPrefs = androidContext().getSharedPreferences(
            "jambo_food_preferences",
            Context.MODE_PRIVATE
        )
        SharedPreferencesSettings(sharedPrefs)
    }

    single<PlatformLocationService> { AndroidLocationService(androidContext()) }
}
