package com.example.sisvitag2.ui.screens.login


import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
// Importa el repositorio correcto y las clases de resultado/error
import com.example.sisvitag2.data.repository.LoginRepository
import com.example.sisvitag2.data.repository.LoginResult
import com.example.sisvitag2.data.repository.LoginError

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Define los estados posibles para la UI de Login
sealed class LoginUiState {
    object Idle : LoginUiState() // Estado inicial
    object Loading : LoginUiState() // Proceso de login en curso
    object Success : LoginUiState() // Login exitoso (puede ser temporal antes de navegar)
    data class Error(val errorType: LoginError, val message: String? = null) : LoginUiState() // Error en el login
}

class LoginViewModel (
    // Inyecta el LoginRepository adaptado a Firebase
    private val loginRepository: LoginRepository
) : ViewModel() {

    // StateFlow para exponer el estado de la UI a la pantalla
    private val _loginUiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val loginUiState: StateFlow<LoginUiState> = _loginUiState.asStateFlow()

    // Ya no necesitamos el StateFlow booleano separado _loginState
    // ni la variable mutable showErrorDialog. El estado se maneja con LoginUiState.

    fun login(email: String, password: String) {
        // Evita múltiples logins si ya está en progreso
        if (_loginUiState.value is LoginUiState.Loading) {
            return
        }

        viewModelScope.launch {
            _loginUiState.value = LoginUiState.Loading // Indica que está cargando
            Log.d("LoginViewModel", "Intentando iniciar sesión con: $email")

            // Llama al repositorio refactorizado
            val result = loginRepository.login(email, password)

            // Actualiza el estado de la UI basado en el resultado del repositorio
            when (result) {
                is LoginResult.Success -> {
                    Log.i("LoginViewModel", "Login exitoso recibido del repositorio.")
                    _loginUiState.value = LoginUiState.Success
                    // Nota: La navegación real generalmente la maneja la UI
                    // al observar este estado Success, o un evento separado.
                }
                is LoginResult.Failure -> {
                    Log.w("LoginViewModel", "Login fallido: ${result.errorType} - ${result.message}")
                    _loginUiState.value = LoginUiState.Error(result.errorType, result.message)
                }
            }
        }
    }

    /**
     * Resetea el estado de la UI a Idle.
     * Útil para llamar después de que la UI maneje un estado de Error o Success.
     */
    fun resetState() {
        _loginUiState.value = LoginUiState.Idle
    }

    // La función logout ya no pertenece estrictamente a este ViewModel.
    // El logout se maneja llamando a FirebaseAuth.signOut() y la UI
    // reacciona al cambio global del estado de autenticación.
    // Si necesitas una acción específica en el ViewModel al hacer logout,
    // podrías tenerla, pero la lógica principal de signOut está fuera.
    /*
    fun performLogoutAction() {
        // Lógica específica del ViewModel si es necesaria al hacer logout
        resetState()
    }
    */
}