package com.example.sisvitag2.ui.screens.loading // Ajusta paquete

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sisvitag2.data.repository.emotionalAnalysis.EmotionalAnalysisRepository
import com.example.sisvitag2.data.repository.emotionalAnalysis.FirestoreAnalysisResult
import com.example.sisvitag2.data.repository.emotionalOrientation.EmotionalOrientationRepository
import com.example.sisvitag2.data.repository.emotionalAnalysis.EmotionalAnalysisResponse // Modelo Emociones
import com.example.sisvitag2.data.repository.emotionalOrientation.EmotionalOrientationResponse // Modelo Orientación

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect // Asegúrate que esté importado
import kotlinx.coroutines.flow.onCompletion // Si se usa
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.update // Para actualizar state de forma segura

// Define los estados posibles de este ViewModel
sealed class AnalysisState {
    object Idle : AnalysisState()
    object Uploading : AnalysisState()
    data class Processing(val videoId: String) : AnalysisState()
    // Asegúrate que AnalysisSuccess tenga videoId y result
    data class AnalysisSuccess(val videoId: String, val result: FirestoreAnalysisResult) : AnalysisState()
    // data class OrientationSuccess(val orientation: EmotionalOrientationResponse) : AnalysisState() // Mantener si usas
    data class Error(val message: String) : AnalysisState()
}

class LoadingViewModel(
    private val analysisRepository: EmotionalAnalysisRepository,
    private val orientationRepository: EmotionalOrientationRepository
) : ViewModel() {

    private val _analysisState = MutableStateFlow<AnalysisState>(AnalysisState.Idle)
    val analysisState: StateFlow<AnalysisState> = _analysisState.asStateFlow()

    companion object {
        private const val TAG = "LoadingViewModel"
    }

    fun startVideoAnalysis(videoUri: Uri) {
        if (_analysisState.value !is AnalysisState.Idle && _analysisState.value !is AnalysisState.Error) {
            Log.w(TAG, "Análisis ya en progreso.")
            return
        }

        viewModelScope.launch {
            _analysisState.value = AnalysisState.Uploading
            Log.d(TAG, "Iniciando subida del video: $videoUri")

            val videoId = analysisRepository.uploadVideoForAnalysis(videoUri)

            if (videoId != null) {
                Log.i(TAG, "Video subido. Iniciando escucha para ID: $videoId")
                // --- CORRECCIÓN 1: Pasar solo videoId a Processing ---
                _analysisState.value = AnalysisState.Processing(videoId) // Correcto
                // -----------------------------------------------------

                analysisRepository.getAnalysisResultsFlow(videoId)
                    .catch { e ->
                        Log.e(TAG, "Error en el Flow de resultados para $videoId", e)
                        // Actualiza el estado de forma segura
                        _analysisState.update { currentState ->
                            if (currentState is AnalysisState.Processing && currentState.videoId == videoId) {
                                AnalysisState.Error("Error al obtener resultados: ${e.message}")
                            } else {
                                currentState // Mantener estado si ya no es Processing para este ID
                            }
                        }
                    }
                    .collect { firestoreDocResult -> // Renombrado para claridad
                        // Verifica que el estado actual corresponda a este videoId
                        // para evitar actualizaciones tardías de un análisis anterior
                        val currentState = _analysisState.value
                        if (currentState is AnalysisState.Processing && currentState.videoId == videoId) {

                            if (firestoreDocResult != null) {
                                Log.d(TAG, "Resultado Firestore recibido para $videoId: Status=${firestoreDocResult.status}")
                                when (firestoreDocResult.status) {
                                    "completado" -> {
                                        if (firestoreDocResult.resultados != null) {
                                            Log.i(TAG, "Análisis completado con éxito para $videoId")
                                            // --- CORRECCIÓN 2: Pasar videoId y firestoreDocResult a AnalysisSuccess ---
                                            _analysisState.value = AnalysisState.AnalysisSuccess(videoId, firestoreDocResult)
                                            // ----------------------------------------------------------------------
                                            // Opcional: Disparar orientación (asegúrate que userName esté disponible)
                                            // val userName = getCurrentUserName() // Necesitarías una forma de obtenerlo
                                            // fetchOrientationData(firestoreDocResult.resultados, userName)
                                        } else {
                                            Log.e(TAG, "Análisis completado pero sin resultados para $videoId")
                                            _analysisState.value = AnalysisState.Error("Análisis completado pero faltan datos.")
                                        }
                                    }
                                    "error" -> {
                                        Log.e(TAG, "Error reportado en Firestore para $videoId: ${firestoreDocResult.error}")
                                        _analysisState.value = AnalysisState.Error(firestoreDocResult.error ?: "Error desconocido durante el análisis.")
                                    }
                                    // "subido", "procesando", "pendiente" -> {
                                    //      Ya estamos en estado Processing, no es necesario cambiar explícitamente
                                    //      a menos que quieras un mensaje más específico en la UI.
                                    // }
                                    else -> {
                                        Log.w(TAG, "Estado desconocido '${firestoreDocResult.status}' recibido para $videoId")
                                        // Podrías mantener Processing o reportar un error leve
                                    }
                                }
                            } else {
                                // Documento no encontrado o eliminado mientras se procesaba
                                Log.w(TAG, "El documento $videoId ya no existe durante el procesamiento.")
                                _analysisState.value = AnalysisState.Error("Se perdió la referencia al análisis.")
                            }
                        } else {
                            Log.w(TAG, "Resultado obsoleto recibido para $videoId mientras el estado es ${_analysisState.value}")
                        }
                    }
            } else {
                Log.e(TAG, "Fallo al subir el video.")
                _analysisState.value = AnalysisState.Error("No se pudo subir el video.")
            }
        }
    }

    // Función de orientación (sin cambios, pero necesita fuente para userName)
    private fun fetchOrientationData(emotions: EmotionalAnalysisResponse, userName: String?) {
        // ... (código existente, asegúrate de obtener userName de alguna parte) ...
        if (userName == null) {
            Log.w(TAG, "Nombre de usuario no disponible para obtener orientación.")
            // Actualizar estado a error específico de orientación?
            // _analysisState.value = AnalysisState.Error("Faltan datos para orientación.")
            return
        }
        viewModelScope.launch {
            try {
                // ... (preparar emotionMap) ...
                val emotionMap = mapOf(
                    "Disgustado" to (emotions.disgusted ?: 0.0),
                    "Enojado" to (emotions.angry ?: 0.0),
                    "Feliz" to (emotions.happy ?: 0.0),
                    "Miedo" to (emotions.scared ?: 0.0),
                    "Neutral" to (emotions.neutral ?: 0.0),
                    "Sorpresa" to (emotions.surprised ?: 0.0),
                    "Triste" to (emotions.sad ?: 0.0)
                )
                val orientation = orientationRepository.getRespuesta(userName, emotionMap)
                // Decide cómo manejar el resultado de la orientación (ej. otro estado, evento)
                Log.d(TAG, "Respuesta de orientación: ${orientation.response}")
            } catch (e: Exception) {
                Log.e(TAG, "Error al obtener orientación", e)
                // Actualizar estado a error específico de orientación?
            }
        }
    }

    fun resetState() {
        // Solo resetea si está en un estado final o error
        if (_analysisState.value !is AnalysisState.Uploading && _analysisState.value !is AnalysisState.Processing) {
            _analysisState.value = AnalysisState.Idle
            Log.d(TAG, "Estado reseteado a Idle.")
        } else {
            Log.w(TAG, "Intento de resetear estado mientras está en progreso: ${_analysisState.value}")
        }
    }
}