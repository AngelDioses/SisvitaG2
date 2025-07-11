package com.example.sisvitag2.data.repository.emotionalAnalysis

import kotlinx.serialization.Serializable

@Serializable
data class EmotionalAnalysisResponse(
    // Nuevo formato de la API: https://detect-emotions-3uyocih3lq-uc.a.run.app
    val angry: Int = 0,
    val disgust: Int = 0,
    val fear: Int = 0,
    val happy: Int = 0,
    val sad: Int = 0,
    val surprise: Int = 0,
    val neutral: Int = 0
) {
    /**
     * Obtiene la emoción dominante basada en los valores más altos
     */
    fun getDominantEmotion(): String {
        val emotions = mapOf(
            "angry" to angry,
            "disgust" to disgust,
            "fear" to fear,
            "happy" to happy,
            "sad" to sad,
            "surprise" to surprise,
            "neutral" to neutral
        )
        
        return emotions.maxByOrNull { it.value }?.key ?: "neutral"
    }
    
    /**
     * Calcula el total de todas las emociones
     */
    fun getTotalEmotions(): Int {
        return angry + disgust + fear + happy + sad + surprise + neutral
    }
    
    /**
     * Convierte los valores a porcentajes
     */
    fun getEmotionPercentages(): Map<String, Double> {
        val total = getTotalEmotions().toDouble()
        if (total == 0.0) return emptyMap()
        
        return mapOf(
            "angry" to (angry / total * 100),
            "disgust" to (disgust / total * 100),
            "fear" to (fear / total * 100),
            "happy" to (happy / total * 100),
            "sad" to (sad / total * 100),
            "surprise" to (surprise / total * 100),
            "neutral" to (neutral / total * 100)
        )
    }
}