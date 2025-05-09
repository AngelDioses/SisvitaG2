package com.example.sisvitag2.util

// Importa el modelo de respuesta de Firebase
import com.example.sisvitag2.data.repository.emotionalAnalysis.EmotionalAnalysisResponse // Ajusta el import si es necesario
import java.math.RoundingMode

/**
 * Calcula el nivel de ansiedad basado en un mapa de emociones y sus porcentajes.
 * Las ponderaciones son un ejemplo y deben ajustarse según los criterios del test/modelo.
 * @param emotions Porcentajes de cada emoción (Map<String, Float>, donde el valor es 0-100).
 * @return Nivel de ansiedad como Float (0-100).
 */
fun calculateAnxietyLevel(emotions: Map<String, Float>): Float {
    // Ponderaciones para cada emoción (ejemplo, ajustar según necesidad)
    val weights = mapOf(
        "Disgustado" to 0.15f,
        "Enojado" to 0.30f,
        "Feliz" to -0.40f, // Feliz reduce la ansiedad
        "Miedo" to 0.40f,
        "Neutral" to -0.10f, // Neutral reduce ligeramente
        "Sorpresa" to 0.10f,
        "Triste" to 0.30f
    )

    var weightedSum = 0f
    // Itera sobre las emociones recibidas
    emotions.forEach { (emotionName, percentage) ->
        // Normaliza el porcentaje a un rango de 0-1 para el cálculo
        val normalizedValue = percentage / 100f
        weightedSum += (weights[emotionName] ?: 0f) * normalizedValue
    }

    // Escala el resultado a 0-100. Ajusta este escalado si tus pesos dan otro rango.
    // Ejemplo: (weightedSum + 1f) * 50f si el rango teórico de la suma es -1 a +1.
    // Mantendremos la lógica de acotar y multiplicar por 100 como estaba antes.
    val anxietyLevel = (weightedSum * 100f).coerceIn(0f, 100f) // Acota entre 0 y 100

    // Redondeo a 3 decimales (opcional)
    return anxietyLevel.toBigDecimal().setScale(3, RoundingMode.HALF_UP).toFloat()
}

/**
 * Convierte los resultados de emociones de Firebase (que pueden ser Doubles 0-1)
 * a un mapa de porcentajes (Float 0-100) con redondeo.
 * @param emotions El objeto EmotionalAnalysisResponse obtenido de Firestore.
 * @return Un Map<String, Float> con los nombres de las emociones y sus porcentajes.
 */
fun mapFirebaseEmotionsToFloatPercentages(emotions: EmotionalAnalysisResponse): Map<String, Float> {
    return mapOf(
        "Disgustado" to ((emotions.disgusted ?: 0.0) * 100).toFloat(),
        "Enojado" to ((emotions.angry ?: 0.0) * 100).toFloat(),
        "Feliz" to ((emotions.happy ?: 0.0) * 100).toFloat(),
        "Miedo" to ((emotions.scared ?: 0.0) * 100).toFloat(),
        "Neutral" to ((emotions.neutral ?: 0.0) * 100).toFloat(),
        "Sorpresa" to ((emotions.surprised ?: 0.0) * 100).toFloat(),
        "Triste" to ((emotions.sad ?: 0.0) * 100).toFloat()
    ).mapValues { (_, value) ->
        // Redondeo a 3 decimales
        value.toBigDecimal().setScale(3, RoundingMode.HALF_UP).toFloat()
    }
}