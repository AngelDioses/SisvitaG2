package com.example.sisvitag2.data.repository.emotionalOrientation

data class EmotionalOrientationResponse(
    val success: Boolean = false, // Valor default
    val message: String = "",     // Valor default
    val response: List<String> = emptyList() // Valor default
)