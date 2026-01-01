package com.example.jambofooddelivery

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.jambofooddelivery.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

class MainApplication : Application() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()

        initKoin {
            androidLogger()
            androidContext(this@MainApplication)
            modules(
                androidModule,

            )
        }

    }
}