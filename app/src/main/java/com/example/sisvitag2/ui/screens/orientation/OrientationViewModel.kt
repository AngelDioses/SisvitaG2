package com.example.sisvitag2.ui.screens.orientation


import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
// Importa el repositorio adaptado a Firebase Functions
import com.example.sisvitag2.data.repository.emotionalOrientation.EmotionalOrientationRepository
// Importa el modelo de respuesta
import com.example.sisvitag2.data.repository.emotionalOrientation.EmotionalOrientationResponse
// Importa el modelo de emociones si lo necesitas pasar
import com.example.sisvitag2.data.repository.emotionalAnalysis.EmotionalAnalysisResponse

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Estados posibles para la UI de Orientación
sealed class OrientationUiState {
    object Idle : OrientationUiState()
    object Loading : OrientationUiState()
    data class Success(val response: EmotionalOrientationResponse) : OrientationUiState()
    data class Error(val message: String) : OrientationUiState()
}

class OrientationViewModel (
    // Inyecta el repositorio adaptado
    private val orientationRepository: EmotionalOrientationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<OrientationUiState>(OrientationUiState.Idle)
    val uiState: StateFlow<OrientationUiState> = _uiState.asStateFlow()

    /**
     * Obtiene la respuesta de orientación llamando a la Cloud Function.
     *
     * @param userName El nombre del usuario (o relevante).
     * @param emotionsData Los datos de emociones (ej. el objeto EmotionalAnalysisResponse o un Map).
     */
    fun fetchOrientation(userName: String?, emotionsData: EmotionalAnalysisResponse?) {
        // Validar entradas
        if (userName.isNullOrBlank()) {
            _uiState.value = OrientationUiState.Error("Nombre de usuario no disponible.")
            return
        }
        if (emotionsData == null) {
            _uiState.value = OrientationUiState.Error("Datos de emociones no disponibles.")
            return
        }
        if (_uiState.value is OrientationUiState.Loading) return // Evitar llamadas múltiples

        _uiState.value = OrientationUiState.Loading
        viewModelScope.launch {
            try {
                Log.d("OrientationViewModel", "Obteniendo orientación para: $userName")
                // Prepara el mapa de emociones como Double
                val emotionMap = mapOf(
                    "Disgustado" to emotionsData.disgust.toDouble(),
                    "Enojado" to emotionsData.angry.toDouble(),
                    "Feliz" to emotionsData.happy.toDouble(),
                    "Miedo" to emotionsData.fear.toDouble(),
                    "Neutral" to emotionsData.neutral.toDouble(),
                    "Sorpresa" to emotionsData.surprise.toDouble(),
                    "Triste" to emotionsData.sad.toDouble()
                )

                // Llama al repositorio (que llama a la Cloud Function)
                val orientationResponse = orientationRepository.getRespuesta(userName, emotionMap)

                if (orientationResponse.success) {
                    Log.i("OrientationViewModel", "Orientación recibida: ${orientationResponse.response.size} mensajes.")
                    _uiState.value = OrientationUiState.Success(orientationResponse)
                } else {
                    Log.w("OrientationViewModel", "La función de orientación devolvió un error: ${orientationResponse.message}")
                    _uiState.value = OrientationUiState.Error(orientationResponse.message.ifBlank { "Error al obtener orientación." })
                }
            } catch (e: Exception) {
                Log.e("OrientationViewModel", "Excepción al obtener orientación", e)
                _uiState.value = OrientationUiState.Error("Error de conexión o inesperado: ${e.message}")
            }
        }
    }

    /**
     * Resetea el estado a Idle.
     */
    fun resetState() {
        _uiState.value = OrientationUiState.Idle
    }
}