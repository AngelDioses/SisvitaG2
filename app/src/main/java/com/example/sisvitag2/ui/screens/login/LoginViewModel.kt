package com.example.sisvitag2.ui.screens.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sisvitag2.data.repository.LoginRepository
import com.example.sisvitag2.data.repository.LoginResult
import com.example.sisvitag2.data.repository.LoginError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    object Success : LoginUiState()
    data class Error(val errorType: LoginError, val message: String? = null) : LoginUiState()
}

class LoginViewModel (
    private val loginRepository: LoginRepository
) : ViewModel() {

    // Función para notificar cambios de autenticación
    var onAuthStateChanged: (() -> Unit)? = null

    private val _loginUiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val loginUiState: StateFlow<LoginUiState> = _loginUiState.asStateFlow()

    fun login(email: String, password: String) {
        if (_loginUiState.value is LoginUiState.Loading) {
            return
        }
        viewModelScope.launch {
            _loginUiState.value = LoginUiState.Loading
            Log.d("LoginViewModel", "Intentando iniciar sesión con: $email")
            val result = loginRepository.login(email, password)
            when (result) {
                is LoginResult.Success -> {
                    Log.i("LoginViewModel", "Login exitoso recibido del repositorio.")
                    _loginUiState.value = LoginUiState.Success
                    // Notificar cambio de estado de autenticación
                    onAuthStateChanged?.invoke()
                }
                is LoginResult.Failure -> {
                    Log.w("LoginViewModel", "Login fallido: ${result.errorType} - ${result.message}")
                    _loginUiState.value = LoginUiState.Error(result.errorType, result.message)
                }
            }
        }
    }

    fun resetState() {
        _loginUiState.value = LoginUiState.Idle
    }
}