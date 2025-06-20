package com.example.sisvitag2.data.model

import com.google.firebase.Timestamp

enum class HistorialTipo {
    TEST_PSICOLOGICO,
    ANALISIS_EMOCIONAL_VIDEO
}

data class HistorialItemPaciente(
    val id: String = "", // Puede ser diagnosticoId o videoId (de analisisResultados)
    val tipo: HistorialTipo = HistorialTipo.TEST_PSICOLOGICO,
    val nombreActividad: String = "Actividad", // Ej. "Test de Ansiedad" o "Análisis Facial del 15/05"
    val fechaRealizacion: Timestamp = Timestamp.now(),
    val tieneFeedback: Boolean = false,
    // Opcional: El feedback directamente aquí si quieres cargarlo de una vez
    // val observacionEspecialista: String? = null,
    // val recomendacionEspecialista: String? = null
    // Opcional: para mostrar el puntaje del test directamente en la lista
    val puntaje: Int? = null,
    val diagnosticoTextoBreve: String? = null // Ej. "Ansiedad Leve"
)

// Modelo para el feedback detallado (cuando el paciente abre un item)
data class FeedbackDetallado(
    val observacion: String?,
    val recomendacion: String?,
    val fechaFeedback: Timestamp?
)