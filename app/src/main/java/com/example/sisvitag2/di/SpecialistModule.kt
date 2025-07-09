package com.example.sisvitag2.di

import com.example.sisvitag2.data.repository.SpecialistRepository
import com.example.sisvitag2.ui.vm.SpecialistViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val specialistModule = module {
    
    // Repositorio del especialista
    single {
        SpecialistRepository(
            firestore = get(),
            auth = get()
        )
    }
    
    // ViewModel del especialista
    viewModel {
        SpecialistViewModel(
            specialistRepository = get()
        )
    }
} 