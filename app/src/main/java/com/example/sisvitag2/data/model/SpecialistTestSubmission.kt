package com.example.sisvitag2.data.model

import com.google.firebase.Timestamp

data class SpecialistTestSubmission(
    val id: String = "",
    val userId: String = "", // ID de la persona que envió el test
    val userName: String = "", // Nombre de la persona
    val testType: String = "", // Tipo de test (ansiedad, depresión, etc.)
    val answers: List<TestAnswer> = emptyList(), // Respuestas del test
    val totalScore: Int = 0, // Puntuación total del test
    val submissionDate: Timestamp = Timestamp.now(),
    val status: TestStatus = TestStatus.PENDING, // Estado del test
    val specialistId: String? = null, // ID del especialista asignado (opcional)
    val specialistName: String? = null, // Nombre del especialista asignado
    val feedbackId: String? = null // ID del feedback si ya fue revisado
)

data class TestAnswer(
    val questionId: Int = 0,
    val questionText: String = "",
    val answer: Int = 0, // Valor de la respuesta (1-5, etc.)
    val answerText: String = "" // Texto de la respuesta si aplica
)

enum class TestStatus {
    PENDING,    // Pendiente de revisión
    IN_REVIEW,  // En revisión por especialista
    COMPLETED,  // Revisado y con feedback
    ARCHIVED    // Archivado
}

enum class TestType {
    ANXIETY,    // Test de ansiedad
    DEPRESSION, // Test de depresión
    STRESS,     // Test de estrés
    // Agregar más tipos según necesites
} 