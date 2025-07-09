package com.example.sisvitag2.di

import com.example.sisvitag2.data.repository.AccountRepository
import com.example.sisvitag2.ui.vm.AccountViewModel
import com.example.sisvitag2.ui.vm.ChangePasswordViewModel
import com.example.sisvitag2.ui.vm.AdminViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val accountModule = module {
    // AccountRepository ahora también necesita FirebaseStorage
    single { AccountRepository(get(), get(), get()) } // Inyecta Auth, Firestore, y Storage
    // AccountViewModel ahora también necesita FirebaseAuth y SessionViewModel
    viewModel { AccountViewModel(get(), get(), get()) }      // Inyecta AccountRepository, FirebaseAuth y SessionViewModel
    // ChangePasswordViewModel
    viewModel { ChangePasswordViewModel() }
    viewModel { AdminViewModel(get()) }
}