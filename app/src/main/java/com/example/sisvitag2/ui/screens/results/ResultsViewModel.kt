package com.example.sisvitag2.ui.screens.results // Ajusta el paquete si es necesario

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
// Importa Repositorios y modelos necesarios
import com.example.sisvitag2.data.repository.emotionalAnalysis.EmotionalAnalysisRepository
import com.example.sisvitag2.data.repository.emotionalAnalysis.FirestoreAnalysisResult
import com.example.sisvitag2.data.repository.emotionalAnalysis.EmotionalAnalysisResponse
import com.example.sisvitag2.util.calculateAnxietyLevel
import com.example.sisvitag2.util.mapFirebaseEmotionsToFloatPercentages
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Define los estados posibles para la UI de la pantalla de Resultados.
 */
sealed class ResultsUiState {
    object Idle : ResultsUiState()
    object Loading : ResultsUiState()
    data class Success(
        val emotions: EmotionalAnalysisResponse, // Garantizado no nulo aquí
        val anxietyLevel: Float,
        val userName: String?
    ) : ResultsUiState()
    data class Error(val message: String) : ResultsUiState()
}

/**
 * ViewModel para la pantalla de Resultados (ResultsScreen).
 */
class ResultsViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val analysisRepository: EmotionalAnalysisRepository,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _uiState = MutableStateFlow<ResultsUiState>(ResultsUiState.Idle)
    val uiState: StateFlow<ResultsUiState> = _uiState.asStateFlow()

    private val videoId: String? = savedStateHandle["videoId"]

    companion object {
        private const val TAG = "ResultsViewModel"
        private const val USERS_COLLECTION = "usuarios"
    }

    init {
        if (!videoId.isNullOrBlank()) {
            loadResults(videoId)
        } else {
            Log.e(TAG, "videoId nulo o vacío en SavedStateHandle.")
            _uiState.value = ResultsUiState.Error("ID de análisis no encontrado.")
        }
    }

    /**
     * Carga los resultados del análisis y el perfil de usuario.
     * @param analysisVideoId El ID del documento 'analisisResultados'.
     */
    fun loadResults(analysisVideoId: String) {
        if (analysisVideoId.isBlank()) {
            _uiState.value = ResultsUiState.Error("ID de análisis inválido.")
            return
        }
        if (_uiState.value is ResultsUiState.Loading) return

        _uiState.value = ResultsUiState.Loading
        viewModelScope.launch {
            try {
                Log.i(TAG, "Cargando resultados para videoId: $analysisVideoId")

                // --- Paso 1: Obtener Resultados del Análisis ---
                val analysisResult: FirestoreAnalysisResult? =
                    analysisRepository.getAnalysisResultById(analysisVideoId)

                if (analysisResult == null) {
                    Log.e(TAG, "Doc análisis no encontrado: $analysisVideoId")
                    _uiState.value = ResultsUiState.Error("No se encontraron resultados.")
                    return@launch
                }
                // Separar comprobación de status y null de resultados
                if (analysisResult.status != "completado") {
                    Log.w(TAG, "Análisis no completado. Status: ${analysisResult.status}, Error: ${analysisResult.error}")
                    _uiState.value = ResultsUiState.Error(analysisResult.error ?: "El análisis no se completó correctamente.")
                    return@launch
                }

                // --- CORRECCIÓN CON VARIABLE LOCAL ---
                val localResultados = analysisResult.resultados
                if (localResultados == null) {
                    Log.e(TAG, "Resultados nulos aunque status es 'completado'. VideoId: $analysisVideoId")
                    _uiState.value = ResultsUiState.Error("Faltan datos en el resultado del análisis.")
                    return@launch
                }
                // A partir de aquí, usamos 'localResultados' que es NO NULO
                val emotions: EmotionalAnalysisResponse = localResultados
                // -------------------------------------
                Log.d(TAG, "Emociones obtenidas con éxito.")

                // --- Paso 2: Obtener Nombre del Usuario ---
                val userId = auth.currentUser?.uid
                var userName: String? = null // Inicializar como null
                if (userId != null) {
                    try {
                        Log.d(TAG, "Obteniendo perfil para UID: $userId")
                        val userDoc = firestore.collection(USERS_COLLECTION)
                            .document(userId).get().await() // await funciona con el import
                        userName = userDoc.getString("nombre") // Obtiene campo "nombre"
                        if (userName == null) {
                            Log.w(TAG, "Campo 'nombre' no encontrado para usuario $userId.")
                            userName = "Usuario" // Default si no existe el campo
                        } else {
                            Log.i(TAG, "Nombre obtenido: $userName")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error al obtener perfil $userId", e)
                        userName = "Usuario" // Default en caso de error
                    }
                } else {
                    Log.w(TAG, "Usuario no autenticado.")
                    userName = "Invitado"
                }

                // --- Paso 3: Calcular Nivel de Ansiedad ---
                // 'emotions' (localResultados) aquí es garantizado no nulo
                val emotionPercentages = mapFirebaseEmotionsToFloatPercentages(emotions)
                val anxietyLevel = calculateAnxietyLevel(emotionPercentages)
                Log.d(TAG, "Ansiedad calculada: $anxietyLevel")

                // --- Paso 4: Actualizar UI a Estado Exitoso ---
                // 'emotions' (localResultados) aquí es garantizado no nulo
                _uiState.value = ResultsUiState.Success(
                    emotions = emotions,
                    anxietyLevel = anxietyLevel,
                    userName = userName
                )
                Log.i(TAG, "Carga completada con éxito para videoId: $analysisVideoId")

            } catch (e: Exception) {
                Log.e(TAG, "Excepción general cargando resultados $analysisVideoId", e)
                _uiState.value = ResultsUiState.Error("Error inesperado al cargar resultados.")
            }
        }
    }

    /**
     * Permite reintentar la carga si falló previamente.
     */
    fun retryLoadResults() {
        if (!videoId.isNullOrBlank()) {
            if (_uiState.value !is ResultsUiState.Loading) {
                Log.d(TAG, "Reintentando carga para videoId: $videoId")
                loadResults(videoId) // Llama a loadResults de nuevo
            }
        } else {
            Log.e(TAG, "Reintento fallido: videoId es nulo.")
            _uiState.value = ResultsUiState.Error("No se puede reintentar: Falta ID del análisis.")
        }
    }
}