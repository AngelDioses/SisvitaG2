package com.example.sisvitag2.data.repository.emotionalAnalysis

// Ya no necesitas @SerializedName de GSON si los nombres de campo en Firestore coinciden
// con los nombres de las propiedades en Kotlin (o si usas @PropertyName de Firestore).
// Es buena práctica usar nombres de campo estándar (ej. camelCase) en Firestore.

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
    // Constructor sin argumentos necesario para la deserialización automática de Firestore.
    // Kotlin genera uno si todos los parámetros del constructor primario tienen valores default.
    // Si no pones valores default arriba, necesitarías:
    // constructor() : this(null, null, null, null, null, null, null)
}