package com.example.sisvitag2.ui.screens.test

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sisvitag2.data.model.Pregunta
import com.example.sisvitag2.data.model.Respuesta
import com.example.sisvitag2.data.model.Test
import com.example.sisvitag2.data.model.TestSubmission
import com.example.sisvitag2.data.repository.TestRepository
import com.example.sisvitag2.data.repository.SubmitTestResult
import com.example.sisvitag2.data.repository.SubmitTestError // Asegúrate que este enum esté definido
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collect
import com.google.firebase.auth.FirebaseAuth

// Añade el nuevo error al enum
// enum SubmitTestError { ALL_QUESTIONS_NOT_ANSWERED, INVALID_RESPONSE, FUNCTION_CALL_FAILED, UNKNOWN }

data class TestUiState(
    val tests: List<Test> = emptyList(),
    val selectedTestId: String? = null,
    val preguntas: List<Pregunta> = emptyList(),
    val respuestas: List<Respuesta> = emptyList(), // Respuestas disponibles para el test seleccionado
    val isLoadingTests: Boolean = false,
    val isLoadingPreguntasRespuestas: Boolean = false,
    val isSubmitting: Boolean = false,
    val submitResult: SubmitTestResult? = null
)

class TestViewModel(
    private val testRepository: TestRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TestUiState())
    val uiState: StateFlow<TestUiState> = _uiState.asStateFlow()

    // Exposición de estados individuales (tu implementación actual)
    val tests: StateFlow<List<Test>> = MutableStateFlow<List<Test>>(emptyList()).also { flow ->
        viewModelScope.launch { _uiState.collect { flow.value = it.tests } }
    }
    val isLoadingTests: StateFlow<Boolean> = MutableStateFlow<Boolean>(false).also { flow ->
        viewModelScope.launch { _uiState.collect { flow.value = it.isLoadingTests } }
    }
    val preguntas: StateFlow<List<Pregunta>> = MutableStateFlow<List<Pregunta>>(emptyList()).also { flow ->
        viewModelScope.launch { _uiState.collect { flow.value = it.preguntas } }
    }
    val respuestas: StateFlow<List<Respuesta>> = MutableStateFlow<List<Respuesta>>(emptyList()).also { flow ->
        viewModelScope.launch { _uiState.collect { flow.value = it.respuestas } }
    }
    val isLoadingPreguntasRespuestas: StateFlow<Boolean> = MutableStateFlow<Boolean>(false).also { flow ->
        viewModelScope.launch { _uiState.collect { flow.value = it.isLoadingPreguntasRespuestas } }
    }
    val isSubmitting: StateFlow<Boolean> = MutableStateFlow<Boolean>(false).also { flow ->
        viewModelScope.launch { _uiState.collect { flow.value = it.isSubmitting } }
    }
    val submitResult: StateFlow<SubmitTestResult?> = MutableStateFlow<SubmitTestResult?>(null).also { flow ->
        viewModelScope.launch { _uiState.collect { flow.value = it.submitResult } }
    }

    init {
        getTests()
    }

    fun getTests() {
        if (_uiState.value.isLoadingTests) return
        _uiState.update { it.copy(isLoadingTests = true) }
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(tests = testRepository.getTests(), isLoadingTests = false) }
            } catch (e: Exception) {
                Log.e("TestViewModel", "Error cargando tests", e)
                _uiState.update { it.copy(isLoadingTests = false) }
            }
        }
    }

    // Esta función selecciona un test y carga sus preguntas y respuestas.
    // TestFormScreen llamará a esta función.
    fun selectTestAndLoadDetails(testId: String?) {
        if (testId == _uiState.value.selectedTestId && testId != null && _uiState.value.preguntas.isNotEmpty()) {
            Log.d("TestViewModel", "Test $testId ya seleccionado y preguntas cargadas.")
            // Si ya está seleccionado y cargado, no hacer nada o solo asegurar que no esté cargando.
            _uiState.update { it.copy(isLoadingPreguntasRespuestas = false) }
            return
        }

        _uiState.update { it.copy(
            selectedTestId = testId,
            preguntas = emptyList(),
            respuestas = emptyList(),
            isLoadingPreguntasRespuestas = testId != null, // Cargar solo si hay testId
            submitResult = null
        )}

        if (!testId.isNullOrBlank()) {
            Log.d("TestViewModel", "Cargando detalles para testId: $testId")
            viewModelScope.launch {
                try {
                    val fetchedPreguntas = testRepository.getPreguntas(testId)
                    val fetchedRespuestas = testRepository.getRespuestas(testId) // Asume que esto obtiene las respuestas para ESE test
                    _uiState.update { it.copy(
                        preguntas = fetchedPreguntas,
                        respuestas = fetchedRespuestas,
                        isLoadingPreguntasRespuestas = false
                    )}
                    Log.d("TestViewModel", "Preguntas: ${fetchedPreguntas.size}, Respuestas: ${fetchedRespuestas.size} para $testId")
                } catch (e: Exception) {
                    Log.e("TestViewModel", "Error cargando preguntas/respuestas para test $testId", e)
                    _uiState.update { it.copy(isLoadingPreguntasRespuestas = false) }
                }
            }
        }
    }

    // La función selectTest original puede que ya no sea tan necesaria si TestFormScreen usa selectTestAndLoadDetails
    // Pero la mantenemos por si TestScreen (la lista) la usa para algo.
    fun selectTest(testId: String?) {
        _uiState.update { it.copy(selectedTestId = testId, submitResult = null) }
        // Si selectTest es solo para marcar el ID sin cargar preguntas inmediatamente, está bien.
        // Si TestFormScreen debe cargar las preguntas al recibir el ID, llamará a selectTestAndLoadDetails.
    }


    fun submitTest(testSubmission: TestSubmission) {
        val currentUiState = _uiState.value
        if (currentUiState.isSubmitting) {
            Log.w("TestViewModel", "Envío ya en progreso.")
            return
        }
        if (testSubmission.testId != currentUiState.selectedTestId || currentUiState.selectedTestId.isNullOrBlank()) {
            Log.e("TestViewModel", "Intento de enviar test incorrecto. Enviado: ${testSubmission.testId}, Seleccionado: ${currentUiState.selectedTestId}")
            _uiState.update { it.copy(submitResult = SubmitTestResult.Failure(SubmitTestError.UNKNOWN, "Error interno: Test inválido.")) }
            return
        }
        // Validación de que todas las preguntas fueron respondidas
        if (currentUiState.preguntas.isNotEmpty() && testSubmission.respuestas.size != currentUiState.preguntas.size) {
            Log.w("TestViewModel", "No todas las preguntas fueron respondidas.")
            _uiState.update { it.copy(submitResult = SubmitTestResult.Failure(SubmitTestError.ALL_QUESTIONS_NOT_ANSWERED, "Por favor, responda todas las preguntas.")) }
            return
        }

        _uiState.update { it.copy(isSubmitting = true, submitResult = null) }
        viewModelScope.launch {
            try {
                // Obtener información del usuario actual
                val user = FirebaseAuth.getInstance().currentUser
                val currentTest = currentUiState.tests.find { it.id == testSubmission.testId }
                
                // Crear TestSubmission con todos los datos necesarios
                val completeTestSubmission = testSubmission.copy(
                    testName = currentTest?.nombre ?: "",
                    userId = user?.uid ?: "",
                    userName = user?.displayName ?: user?.email ?: "",
                    userEmail = user?.email ?: ""
                )
                
                Log.d("TestViewModel", "Enviando TestSubmission completo: $completeTestSubmission")
                val result = testRepository.submitTest(completeTestSubmission)
            Log.d("TestViewModel", "Resultado del envío: $result")
            _uiState.update { it.copy(submitResult = result, isSubmitting = false) }
                
            } catch (e: Exception) {
                Log.e("TestViewModel", "Error al enviar test: ${e.message}")
                _uiState.update { it.copy(
                    submitResult = SubmitTestResult.Failure(SubmitTestError.FUNCTION_CALL_FAILED, e.message),
                    isSubmitting = false
                ) }
            }
        }
    }

    fun clearSubmitResult() {
        if (_uiState.value.submitResult != null) {
            _uiState.update { it.copy(submitResult = null) }
        }
    }

    fun clearCache() { // Sin cambios
        testRepository.clearCache()
        // getTests() // Opcional: recargar
    }
}