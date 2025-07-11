package com.example.sisvitag2.data.model

import com.google.firebase.Timestamp

// Modelo para el feedback del especialista sobre el análisis emocional

data class EmotionalAnalysisFeedback(
    val id: String = "",
    val submissionId: String = "",
    val specialistId: String = "",
    val specialistName: String = "",
    val userId: String = "",
    val feedbackDate: Timestamp = Timestamp.now(),
    val recommendation: String = "",
    val notes: String = ""
) 