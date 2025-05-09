package com.example.sisvitag2.data.repository.emotionalOrientation // Ajusta paquete

import com.example.sisvitag2.data.repository.emotionalOrientation.EmotionalOrientationResponse // <-- ¡IMPORTANTE! Ajusta esta ruta
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.tasks.await

// --- ELIMINADA LA DEFINICIÓN DE DATA CLASS DE AQUÍ ---

class EmotionalOrientationRepository(
    private val functions: FirebaseFunctions,
    private val firebaseAuth: FirebaseAuth
) {

    companion object {
        private const val TAG = "EmotionalOrientationRepo"
        private const val FUNCTION_NAME = "generarRespuestaEmocional"
    }

    suspend fun getRespuesta(nombre: String, emociones: Map<String, Double>): EmotionalOrientationResponse {
        val data = hashMapOf(
            "nombre" to nombre,
            "emociones" to emociones
        )
        Log.d(TAG, "Llamando a Cloud Function: $FUNCTION_NAME con datos: $data")
        return try {
            val result = functions.getHttpsCallable(FUNCTION_NAME).call(data).await()
            val resultMap = result.data as? Map<String, Any>
            if (resultMap != null) {
                Log.d(TAG, "Respuesta recibida: $resultMap")
                // Usa la clase importada
                EmotionalOrientationResponse(
                    success = resultMap["success"] as? Boolean ?: false,
                    message = resultMap["message"] as? String ?: "Mensaje no encontrado",
                    response = (resultMap["response"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
                )
            } else {
                Log.e(TAG, "Respuesta inválida: ${result.data}")
                EmotionalOrientationResponse(success = false, message = "Error: Respuesta inesperada.", response = emptyList())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error llamando a $FUNCTION_NAME", e)
            EmotionalOrientationResponse(success = false, message = "Error al contactar: ${e.message}", response = emptyList())
        }
    }
}