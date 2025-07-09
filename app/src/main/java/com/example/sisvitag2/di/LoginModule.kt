package com.example.sisvitag2.di

import com.example.sisvitag2.data.repository.LoginRepository
import com.example.sisvitag2.ui.screens.login.LoginViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val loginModule = module {
    // Repositorio: Ahora recibe FirebaseAuth y FirebaseFirestore
    single { LoginRepository(get(), get()) } // Koin inyectará FirebaseAuth y FirebaseFirestore

    // ViewModel: Correcto (necesita LoginRepository)
    viewModel { LoginViewModel(get()) }
}