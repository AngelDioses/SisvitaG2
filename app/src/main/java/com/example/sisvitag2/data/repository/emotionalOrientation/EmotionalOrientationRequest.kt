package com.example.sisvitag2.data.repository.emotionalOrientation

data class EmotionalOrientationRequest(
    val nombre: String,
    // Cambiado a Double para mantener consistencia con EmotionalAnalysisResponse
    // y porque los números en JSON/Firestore suelen ser Doubles.
    val emociones: Map<String, Double>
)