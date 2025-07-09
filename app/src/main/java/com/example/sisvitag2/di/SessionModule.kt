package com.example.sisvitag2.di

import com.example.sisvitag2.ui.vm.EmailVerificationViewModel // IMPORTAR
import com.example.sisvitag2.ui.vm.ForgotPasswordViewModel   // IMPORTAR
import com.example.sisvitag2.ui.vm.SessionViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val sessionModule = module {
    // Definición para SessionViewModel como SINGLE para instancia única global
    single { SessionViewModel(get(), get()) } // Koin inyectará FirebaseAuth y FirebaseFirestore

    viewModel { EmailVerificationViewModel(get()) } // Necesita FirebaseAuth
    viewModel { ForgotPasswordViewModel(get()) }   // Necesita FirebaseAuth
}