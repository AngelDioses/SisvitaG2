package com.example.sisvitag2

import android.app.Application
import com.example.sisvitag2.di.emotionOrientationModule
import com.example.sisvitag2.di.emotionalAnalysisModule
import com.example.sisvitag2.di.firebaseModule
import com.example.sisvitag2.di.loginModule
import com.example.sisvitag2.di.registerModule
import com.example.sisvitag2.di.sessionModule // Este módulo ahora define los 3 ViewModels de sesión/auth
import com.example.sisvitag2.di.testModule

import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class SisvitaApp : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger(Level.DEBUG)
            androidContext(this@SisvitaApp)
            modules(
                firebaseModule,
                sessionModule, // Ahora este módulo es más completo
                loginModule,
                registerModule,
                testModule,
                emotionOrientationModule,
                emotionalAnalysisModule
            )
        }
    }
}