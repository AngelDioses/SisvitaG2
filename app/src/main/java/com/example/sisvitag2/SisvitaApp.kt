package com.example.sisvitag2

import android.app.Application
import com.example.sisvitag2.di.emotionOrientationModule
import com.example.sisvitag2.di.emotionalAnalysisModule
import com.example.sisvitag2.di.firebaseModule
import com.example.sisvitag2.di.loginModule
import com.example.sisvitag2.di.registerModule
import com.example.sisvitag2.di.sessionModule // IMPORTANTE
import com.example.sisvitag2.di.testModule
// import com.example.sisvitag2.di.viewModelModule // Si tienes un módulo general de VMs

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
                sessionModule, // Carga el módulo del SessionViewModel
                loginModule,
                registerModule,
                testModule,
                emotionOrientationModule,
                emotionalAnalysisModule
                // viewModelModule // Si tienes uno general
            )
        }
    }
}