import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.sqldelight)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }


    sourceSets {
        all {
            languageSettings.optIn("kotlin.time.ExperimentalTime")
        }

        commonMain.dependencies {

            // Coroutines
            implementation(libs.kotlinx.coroutines.core)

            // Ktor Client
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.client.auth)

            // DateTime
            implementation(libs.kotlinx.datetime)

            // Kotlinx Serialization
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
            
            // SQLDelight
            implementation(libs.runtime)
            implementation("app.cash.sqldelight:coroutines-extensions:${libs.versions.sqlDelight.get()}")

            // Koin DI
            implementation(libs.koin.core)


            // russhwolf
            implementation("com.russhwolf:multiplatform-settings:1.2.0")
            implementation("com.russhwolf:multiplatform-settings-coroutines:1.2.0")

            // Logger
            implementation("io.github.aakira:napier:2.7.1")

            // Cloudinary URL Gen (Common)
            implementation("com.cloudinary:kotlin-url-gen:1.7.0")

            // Socket.IO KMP (Standard Production Library)
//            api(libs.socketio.kmp)
//            implementation("io.socket:socket.io-client:2.1.0")
            implementation("dev.icerock.moko:socket-io:0.6.0")

        }
        androidMain.dependencies {
            implementation(libs.tehras.charts)

            // Ktor Android
            implementation(libs.ktor.client.android)

            //koin
            implementation("io.insert-koin:koin-android:3.5.6")

            // SQLDelight Android Driver
            implementation(libs.android.driver)

            
            // Location Services
            implementation("com.google.android.gms:play-services-location:21.3.0")

            // Firebase
            implementation(project.dependencies.platform("com.google.firebase:firebase-bom:33.7.0"))
            implementation("com.google.firebase:firebase-analytics")
            implementation("com.google.firebase:firebase-crashlytics")
            implementation("com.google.firebase:firebase-messaging")

            // Paystack Android SDK
            implementation(libs.paystack.android)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
            implementation(libs.native.driver)

        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(kotlin("test-common"))
            implementation(kotlin("test-annotations-common"))
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:${libs.versions.coroutinesVersion.get()}")
        }
    }

}



android {
    namespace = "com.jambofooddelivery.shared"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

sqldelight {
    databases {
        create("AppDatabase") {
            packageName.set("com.jambofooddelivery.cache")
        }
    }
}
