package com.example.sisvitag2.ui.vm

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sisvitag2.data.model.FeedbackDetallado
import com.example.sisvitag2.data.model.HistorialItemPaciente
import com.example.sisvitag2.data.model.HistorialTipo
import com.example.sisvitag2.data.repository.HistoryRepository
import com.google.firebase.auth.FirebaseAuth // Para obtener el UID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HistoryUiState(
    val isLoading: Boolean = false,
    val historialItems: List<HistorialItemPaciente> = emptyList(),
    val error: String? = null,
    val selectedItemFeedback: FeedbackDetallado? = null,
    val isLoadingFeedback: Boolean = false
)

class HistoryViewModel(
    private val historyRepository: HistoryRepository,
    private val auth: FirebaseAuth // Para obtener el UID del paciente actual
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    companion object {
        private const val TAG = "HistoryViewModel"
    }

    init {
        loadHistory()
    }

    fun loadHistory(days: Int = 30) {
        if (_uiState.value.isLoading) return
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            val result = historyRepository.getHistorialPaciente(days)
            result.fold(
                onSuccess = { items ->
                    Log.d(TAG, "Historial cargado: ${items.size} items.")
                    _uiState.update { it.copy(isLoading = false, historialItems = items) }
                },
                onFailure = { exception ->
                    Log.e(TAG, "Error cargando historial", exception)
                    _uiState.update { it.copy(isLoading = false, error = exception.message ?: "Error al cargar historial.") }
                }
            )
        }
    }

    fun loadFeedbackForItem(item: HistorialItemPaciente) {
        val currentUserUid = auth.currentUser?.uid
        if (currentUserUid == null) {
            _uiState.update { it.copy(error = "Usuario no autenticado para ver feedback.") }
            return
        }
        if (_uiState.value.isLoadingFeedback) return

        // Usar el id del item como feedbackId solo si tieneFeedback es true
        val feedbackId = if (item.tieneFeedback) item.id else null
        if (feedbackId == null) {
            _uiState.update { it.copy(selectedItemFeedback = null, isLoadingFeedback = false) }
            return
        }

        _uiState.update { it.copy(isLoadingFeedback = true, selectedItemFeedback = null, error = null) }
        viewModelScope.launch {
            val result = historyRepository.getFeedbackDetallado(feedbackId, item.tipo, currentUserUid)
            result.fold(
                onSuccess = { feedback ->
                    _uiState.update { it.copy(isLoadingFeedback = false, selectedItemFeedback = feedback) }
                    if (feedback == null) {
                        Log.d(TAG, "No se encontró feedback para el ítem: ${item.id}")
                    } else {
                        Log.d(TAG, "Feedback cargado para el ítem: ${item.id}")
                    }
                },
                onFailure = { exception ->
                    Log.e(TAG, "Error cargando feedback para ${item.id}", exception)
                    _uiState.update { it.copy(isLoadingFeedback = false, error = "No se pudo cargar el feedback.") }
                }
            )
        }
    }

    fun clearSelectedFeedback() {
        _uiState.update { it.copy(selectedItemFeedback = null, isLoadingFeedback = false) }
    }
}