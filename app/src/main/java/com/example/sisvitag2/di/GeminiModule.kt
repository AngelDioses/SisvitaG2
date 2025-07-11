package com.example.sisvitag2.di

import com.example.sisvitag2.data.repository.gemini.GeminiRepository
import org.koin.dsl.module

val geminiModule = module {
    single { GeminiRepository() }
} 