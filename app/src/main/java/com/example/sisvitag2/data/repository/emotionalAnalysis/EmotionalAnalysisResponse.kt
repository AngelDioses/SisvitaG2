package com.example.sisvitag2.data.repository.emotionalAnalysis



data class EmotionalAnalysisResponse(
    // Usaremos nombres de campo estilo camelCase para Firestore (recomendado)
    val disgusted: Double? = null, // Valor default null (o 0.0)
    val angry: Double? = null,
    val happy: Double? = null,
    val scared: Double? = null,
    val neutral: Double? = null,
    val surprised: Double? = null,
    val sad: Double? = null
) {

}