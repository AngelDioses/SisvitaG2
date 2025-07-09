package com.example.sisvitag2.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sisvitag2.data.model.SpecialistFeedback
import com.example.sisvitag2.data.repository.FeedbackRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import android.util.Log

sealed class FeedbackUiState {
    object Loading : FeedbackUiState()
    data class Success(val feedbacks: List<SpecialistFeedback>) : FeedbackUiState()
    data class Error(val message: String) : FeedbackUiState()
}

class FeedbackViewModel(private val repository: FeedbackRepository) : ViewModel() {
    private val _uiState = MutableStateFlow<FeedbackUiState>(FeedbackUiState.Loading)
    val uiState: StateFlow<FeedbackUiState> = _uiState

    fun loadFeedbacks() {
        _uiState.value = FeedbackUiState.Loading
        viewModelScope.launch {
            try {
                Log.d("FeedbackViewModel", "Cargando feedbacks para usuario actual...")
                val feedbacks = repository.getReceivedFeedbacks()
                Log.d("FeedbackViewModel", "Feedbacks recibidos: ${feedbacks.size}")
                feedbacks.forEach { Log.d("FeedbackViewModel", "Feedback ID: ${it.id}, userId: ${it.userId}") }
                _uiState.value = FeedbackUiState.Success(feedbacks)
            } catch (e: Exception) {
                Log.e("FeedbackViewModel", "Error al obtener feedbacks: ${e.message}")
                _uiState.value = FeedbackUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }
} 