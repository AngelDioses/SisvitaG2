package com.example.sisvitag2.di

import com.example.sisvitag2.ui.vm.SessionViewModel // Asegúrate que la ruta sea correcta
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val sessionModule = module {
    viewModel { SessionViewModel(get(), get()) } // Koin inyectará FirebaseAuth y FirebaseFirestore
}