package com.example.sisvitag2.di

import com.example.sisvitag2.data.repository.TestRepository
import com.example.sisvitag2.ui.screens.test.TestViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val testModule = module {
    // Repositorio: Solo necesita Firestore (eliminamos Functions)
    single { TestRepository(get()) } // Inyecta solo Firestore

    // ViewModel: Correcto (necesita TestRepository)
    viewModel { TestViewModel(get()) } // 'get()' inyecta TestRepository
}