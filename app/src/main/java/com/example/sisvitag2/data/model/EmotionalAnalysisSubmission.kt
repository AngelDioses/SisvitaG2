package com.example.sisvitag2.data.model

import com.google.firebase.Timestamp

// Modelo para el análisis emocional enviado por el usuario

data class EmotionalAnalysisSubmission(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val apellidoPaterno: String = "",
    val fechaNacimiento: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    val results: Map<String, Int> = emptyMap(), // angry, happy, etc.
    val dashboardImageUrl: String? = null, // Si se guarda imagen del dashboard
    val status: String = "pending", // "pending" o "reviewed"
    val feedbackId: String? = null // ID del feedback del especialista
) 