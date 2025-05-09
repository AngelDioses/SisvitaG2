package com.example.sisvitag2.di

// Importa la implementación correcta del repositorio
import com.example.sisvitag2.data.repository.emotionalAnalysis.EmotionalAnalysisRepository
// Importa los ViewModels
import com.example.sisvitag2.ui.screens.loading.LoadingViewModel
import com.example.sisvitag2.ui.screens.results.ResultsViewModel
// Importa el otro repo porque LoadingViewModel lo necesita
import com.example.sisvitag2.data.repository.emotionalOrientation.EmotionalOrientationRepository

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

// Considera renombrar a 'analysisResultsModule' o similar si define más que solo análisis
val emotionalAnalysisModule = module {

    // --- Repositorio ---
    // Define EmotionalAnalysisRepository (Correcto)
    single { EmotionalAnalysisRepository(get(), get(), get()) } // Inyecta Auth, Firestore, Storage

    // --- ViewModels ---
    // Define LoadingViewModel (Correcto, asumiendo que necesita ambos repositorios)
    viewModel { LoadingViewModel(get(), get()) } // Inyecta AnalysisRepo y OrientationRepo

    // Define ResultsViewModel (Correcto)
    // Koin inyecta SavedStateHandle automáticamente
    // Necesita AnalysisRepo, Auth, Firestore
    viewModel { ResultsViewModel(get(), get(), get(), get()) }
}