package com.example.sisvitag2.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sisvitag2.data.model.SpecialistFeedback
import com.example.sisvitag2.data.repository.FeedbackRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import android.util.Log
import com.example.sisvitag2.data.model.EmotionalAnalysisFeedback
import com.example.sisvitag2.data.repository.emotionalAnalysis.EmotionalAnalysisRepository

sealed class FeedbackUiState {
    object Loading : FeedbackUiState()
    data class Success(val feedbacks: List<SpecialistFeedback>) : FeedbackUiState()
    data class Error(val message: String) : FeedbackUiState()
}

sealed class EmotionalAnalysisFeedbackUiState {
    object Loading : EmotionalAnalysisFeedbackUiState()
    data class Success(val feedbacks: List<EmotionalAnalysisFeedback>) : EmotionalAnalysisFeedbackUiState()
    data class Error(val message: String) : EmotionalAnalysisFeedbackUiState()
}

class FeedbackViewModel(
    private val repository: FeedbackRepository,
    private val emotionalAnalysisRepository: EmotionalAnalysisRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<FeedbackUiState>(FeedbackUiState.Loading)
    val uiState: StateFlow<FeedbackUiState> = _uiState

    private val _emotionalFeedbackUiState = MutableStateFlow<EmotionalAnalysisFeedbackUiState>(EmotionalAnalysisFeedbackUiState.Loading)
    val emotionalFeedbackUiState: StateFlow<EmotionalAnalysisFeedbackUiState> = _emotionalFeedbackUiState

    fun loadFeedbacks() {
        _uiState.value = FeedbackUiState.Loading
        viewModelScope.launch {
            try {
                Log.d("FeedbackViewModel", "Cargando feedbacks para usuario actual...")
                val feedbacks = repository.getReceivedFeedbacks()
                Log.d("FeedbackViewModel", "Feedbacks recibidos: "+feedbacks.size)
                feedbacks.forEach { Log.d("FeedbackViewModel", "Feedback ID: "+it.id+", userId: "+it.userId) }
                _uiState.value = FeedbackUiState.Success(feedbacks)
            } catch (e: Exception) {
                Log.e("FeedbackViewModel", "Error al obtener feedbacks: "+e.message)
                _uiState.value = FeedbackUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun loadEmotionalAnalysisFeedbacks(userId: String) {
        _emotionalFeedbackUiState.value = EmotionalAnalysisFeedbackUiState.Loading
        viewModelScope.launch {
            try {
                val feedbacks = emotionalAnalysisRepository.getUserEmotionalAnalysisFeedbacks(userId)
                _emotionalFeedbackUiState.value = EmotionalAnalysisFeedbackUiState.Success(feedbacks)
            } catch (e: Exception) {
                _emotionalFeedbackUiState.value = EmotionalAnalysisFeedbackUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }
} 