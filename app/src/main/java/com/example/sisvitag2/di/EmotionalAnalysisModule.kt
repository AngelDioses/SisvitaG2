package com.example.sisvitag2.di

// Importa la implementación correcta del repositorio
import com.example.sisvitag2.data.repository.emotionalAnalysis.EmotionalAnalysisRepository
import com.example.sisvitag2.data.repository.emotionalAnalysis.EmotionalAnalysisApiService
// Importa los ViewModels
import com.example.sisvitag2.ui.screens.loading.LoadingViewModel
import com.example.sisvitag2.ui.screens.results.ResultsViewModel
import com.example.sisvitag2.ui.screens.camera.CameraScreenViewModel
// Importa el otro repo porque LoadingViewModel lo necesita
import com.example.sisvitag2.data.repository.emotionalOrientation.EmotionalOrientationRepository

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

// Considera renombrar a 'analysisResultsModule' o similar si define más que solo análisis
val emotionalAnalysisModule = module {

    // --- Servicio de API ---
    // Define el servicio de API para la nueva API de detección de emociones
    single { EmotionalAnalysisApiService() }

    // --- Repositorio ---
    // Define EmotionalAnalysisRepository (Actualizado para incluir el servicio de API)
    single { EmotionalAnalysisRepository(get(), get(), get(), get()) } // Inyecta Auth, Firestore, Storage, ApiService

    // --- ViewModels ---
    // Define LoadingViewModel (Correcto, asumiendo que necesita ambos repositorios)
    viewModel { LoadingViewModel(get(), get()) } // Inyecta AnalysisRepo y OrientationRepo

    // Define ResultsViewModel (Correcto)
    // Koin inyecta SavedStateHandle automáticamente
    // Necesita AnalysisRepo, Auth, Firestore
    viewModel { ResultsViewModel(get(), get(), get(), get()) }
    
    // Define CameraScreenViewModel
    viewModel { CameraScreenViewModel(get()) } // Inyecta Application
}