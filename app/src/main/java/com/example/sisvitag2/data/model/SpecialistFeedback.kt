package com.example.sisvitag2.data.model

import com.google.firebase.Timestamp

data class SpecialistFeedback(
    val id: String = "",
    val testSubmissionId: String = "", // ID del test al que responde
    val specialistId: String = "", // ID del especialista
    val specialistName: String = "", // Nombre del especialista
    val userId: String = "", // ID de la persona que recibirá el feedback
    val userName: String = "", // Nombre de la persona
    val testType: String = "", // Tipo de test revisado
    val feedbackDate: Timestamp = Timestamp.now(),
    val assessment: String = "", // Evaluación del especialista
    val recommendations: List<String> = emptyList(), // Lista de recomendaciones
    val severity: FeedbackSeverity = FeedbackSeverity.MILD, // Severidad del caso
    val isUrgent: Boolean = false, // Si requiere atención urgente
    val followUpDate: Timestamp? = null, // Fecha de seguimiento si aplica
    val notes: String = "" // Notas adicionales del especialista
)

enum class FeedbackSeverity {
    MILD,       // Leve
    MODERATE,   // Moderado
    SEVERE,     // Severo
    CRITICAL    // Crítico
} 