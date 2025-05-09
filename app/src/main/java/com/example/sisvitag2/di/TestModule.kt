package com.example.sisvitag2.di

import com.example.sisvitag2.data.repository.TestRepository
import com.example.sisvitag2.ui.screens.test.TestViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val testModule = module {
    // Repositorio: Correcto (necesita Firestore y Functions)
    single { TestRepository(get(), get()) } // Inyecta Firestore y Functions

    // ViewModel: Correcto (necesita TestRepository)
    viewModel { TestViewModel(get()) } // 'get()' inyecta TestRepository
}