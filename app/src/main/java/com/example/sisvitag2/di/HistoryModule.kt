package com.example.sisvitag2.di

import com.example.sisvitag2.data.repository.HistoryRepository
import com.example.sisvitag2.ui.vm.HistoryViewModel // Asegúrate que la ruta sea correcta
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val historyModule = module {
    single { HistoryRepository(get(), get()) } // Necesita Firestore y Auth
    viewModel { HistoryViewModel(get(), get()) }      // Necesita HistoryRepository y FirebaseAuth
}