package com.example.sisvitag2.di


import com.example.sisvitag2.data.repository.RegisterRepository
import com.example.sisvitag2.ui.screens.register.RegisterViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val registerModule = module {
    // Repositorio: Necesita FirebaseAuth y FirebaseFirestore
    single { RegisterRepository(get(), get()) } // Inyecta Auth y Firestore

    // ViewModel: Necesita RegisterRepository
    viewModel { RegisterViewModel(get()) } // Inyecta RegisterRepository
}