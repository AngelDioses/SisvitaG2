package com.example.sisvitag2.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sisvitag2.data.repository.SpecialistRepository
import com.example.sisvitag2.data.model.SpecialistTestSubmission
import com.example.sisvitag2.data.model.SpecialistFeedback
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.util.Log
import com.example.sisvitag2.data.model.EmotionalAnalysisSubmission

data class SpecialistUiState(
    val specialistName: String? = null,
    val pendingTestsCount: Int = 0,
    val completedTodayCount: Int = 0,
    val pendingTests: List<SpecialistTestSubmission> = emptyList(),
    val pendingEmotionalAnalyses: List<EmotionalAnalysisSubmission> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class SpecialistViewModel(
    private val specialistRepository: SpecialistRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SpecialistUiState(isLoading = true))
    val uiState: StateFlow<SpecialistUiState> = _uiState.asStateFlow()
    
    private val _feedbackHistory = MutableStateFlow<List<SpecialistFeedback>>(emptyList())
    val feedbackHistory: StateFlow<List<SpecialistFeedback>> = _feedbackHistory.asStateFlow()

    init {
        Log.d("SpecialistViewModel", "INIT SpecialistViewModel con UID: ${specialistRepository.getCurrentSpecialistUid()}")
        loadSpecialistData()
    }
    
    private fun loadSpecialistData() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                
                // Cargar datos del especialista
                val specialistData = specialistRepository.getSpecialistData()
                val pendingTests = specialistRepository.getPendingTestsCount()
                val completedToday = specialistRepository.getCompletedTodayCount()
                
                _uiState.value = SpecialistUiState(
                    specialistName = specialistData?.nombre,
                    pendingTestsCount = pendingTests,
                    completedTodayCount = completedToday,
                    pendingTests = emptyList(),
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error al cargar datos del especialista"
                )
            }
        }
    }
    
    fun refreshData() {
        loadSpecialistData()
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun loadPendingTests() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                
                val pendingTests = specialistRepository.getPendingTests()
                
                _uiState.value = _uiState.value.copy(
                    pendingTests = pendingTests,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error al cargar tests pendientes"
                )
            }
        }
    }

    fun loadFeedbackHistory() {
        viewModelScope.launch {
            try {
                val specialistId = specialistRepository.getCurrentSpecialistUid()
                Log.d("SpecialistViewModel", "UID autenticado: $specialistId")
                val feedbacks = specialistRepository.getFeedbackHistory()
                Log.d("SpecialistViewModel", "Feedbacks recibidos: ${feedbacks.size}")
                feedbacks.forEach { Log.d("SpecialistViewModel", "Feedback ID: ${it.id}, specialistId: ${it.specialistId}") }
                _feedbackHistory.value = feedbacks
            } catch (e: Exception) {
                Log.e("SpecialistViewModel", "Error al obtener feedbacks: ${e.message}")
                _feedbackHistory.value = emptyList()
            }
        }
    }

    fun loadPendingEmotionalAnalyses() {
        viewModelScope.launch {
            try {
                android.util.Log.d("SpecialistViewModel", "Cargando análisis emocionales pendientes...")
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                val pendingAnalyses = specialistRepository.getPendingEmotionalAnalyses()
                android.util.Log.d("SpecialistViewModel", "Análisis recuperados: ${pendingAnalyses.size}")
                pendingAnalyses.forEach { a ->
                    android.util.Log.d("SpecialistViewModel", "ID: ${a.id}, userName: ${a.userName}, status: ${a.status}")
                }
                _uiState.value = _uiState.value.copy(
                    pendingEmotionalAnalyses = pendingAnalyses,
                    isLoading = false
                )
            } catch (e: Exception) {
                android.util.Log.e("SpecialistViewModel", "Error al cargar análisis emocionales pendientes: ${e.message}", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error al cargar análisis emocionales pendientes"
                )
            }
        }
    }
} 