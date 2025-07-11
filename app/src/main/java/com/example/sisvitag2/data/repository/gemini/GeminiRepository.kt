package com.example.sisvitag2.data.repository.gemini

import com.example.sisvitag2.data.repository.emotionalAnalysis.EmotionalAnalysisResponse
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class GeminiRepository {
    private val apiKey = "AIzaSyDxWQqQ6tW1727eFejS4sEos6GBhXLAYwo"
    
    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = apiKey,
        generationConfig = generationConfig {
            temperature = 0.7f
            topK = 40
            topP = 0.95f
            maxOutputTokens = 1024
        }
    )
    
    fun generateRecommendations(emotionResults: EmotionalAnalysisResponse): Flow<Result<String>> = flow {
        try {
            val prompt = buildRecommendationPrompt(emotionResults)
            
            val response = withContext(Dispatchers.IO) {
                generativeModel.generateContent(prompt)
            }
            
            val recommendation = response.text ?: "No se pudo generar una recomendación."
            emit(Result.success(recommendation))
            
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
    
    private fun buildRecommendationPrompt(results: EmotionalAnalysisResponse): String {
        val dominantEmotion = results.getDominantEmotion()
        val total = results.getTotalEmotions()
        
        return """
        Eres un psicólogo experto en salud mental y bienestar emocional. 
        
        Basándote en el siguiente análisis de emociones de una persona, genera una recomendación personalizada, 
        práctica y útil para mejorar su bienestar emocional. La recomendación debe ser:
        
        - Empatética y comprensiva
        - Práctica y accionable
        - Basada en evidencia científica
        - Enfocada en el bienestar general
        - Escrita en español
        - Máximo 3-4 párrafos
        
        Resultados del análisis:
        - Emoción dominante: ${dominantEmotion.capitalize()}
        - Feliz: ${results.happy} (${if (total > 0) (results.happy.toFloat() / total * 100) else 0}%)
        - Triste: ${results.sad} (${if (total > 0) (results.sad.toFloat() / total * 100) else 0}%)
        - Enojado: ${results.angry} (${if (total > 0) (results.angry.toFloat() / total * 100) else 0}%)
        - Miedo: ${results.fear} (${if (total > 0) (results.fear.toFloat() / total * 100) else 0}%)
        - Sorpresa: ${results.surprise} (${if (total > 0) (results.surprise.toFloat() / total * 100) else 0}%)
        - Disgusto: ${results.disgust} (${if (total > 0) (results.disgust.toFloat() / total * 100) else 0}%)
        - Neutral: ${results.neutral} (${if (total > 0) (results.neutral.toFloat() / total * 100) else 0}%)
        
        Por favor, genera una recomendación personalizada que ayude a esta persona a:
        1. Comprender mejor sus emociones actuales
        2. Desarrollar estrategias para manejar la emoción dominante
        3. Promover un mayor equilibrio emocional
        4. Incluir actividades o prácticas específicas que pueda realizar
        
        La recomendación debe ser motivadora y esperanzadora, evitando un tono clínico o diagnóstico.
        """.trimIndent()
    }
} 