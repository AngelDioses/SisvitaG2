package com.example.sisvitag2.di

import com.example.sisvitag2.data.repository.FeedbackRepository
import com.example.sisvitag2.ui.vm.FeedbackViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val feedbackModule = module {
    single { FeedbackRepository(get(), get()) }
    viewModel { FeedbackViewModel(get(), get()) }
} 