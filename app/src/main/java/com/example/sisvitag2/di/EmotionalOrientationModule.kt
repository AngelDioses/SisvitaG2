package com.example.sisvitag2.di

import com.example.sisvitag2.data.repository.emotionalOrientation.EmotionalOrientationRepository
import com.example.sisvitag2.ui.screens.orientation.OrientationViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val emotionOrientationModule = module {
    // Repositorio: Correcto (necesita Functions y Auth)
    single { EmotionalOrientationRepository(get(), get()) } // Inyecta Functions y Auth

    // ViewModel: Correcto (necesita OrientationRepository)
    viewModel { OrientationViewModel(get()) } // 'get()' inyecta EmotionalOrientationRepository
}