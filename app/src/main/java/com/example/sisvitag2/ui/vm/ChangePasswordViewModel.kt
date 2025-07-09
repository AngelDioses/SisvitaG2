package com.example.sisvitag2.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class ChangePasswordUiState {
    object Idle : ChangePasswordUiState()
    object Loading : ChangePasswordUiState()
    data class Success(val message: String) : ChangePasswordUiState()
    data class Error(val message: String) : ChangePasswordUiState()
}

class ChangePasswordViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    
    private val _uiState = MutableStateFlow<ChangePasswordUiState>(ChangePasswordUiState.Idle)
    val uiState: StateFlow<ChangePasswordUiState> = _uiState.asStateFlow()
    
    fun changePassword(currentPassword: String, newPassword: String, confirmPassword: String) {
        if (!validateInputs(currentPassword, newPassword, confirmPassword)) {
            return
        }
        
        viewModelScope.launch {
            _uiState.value = ChangePasswordUiState.Loading
            
            try {
                val user = auth.currentUser
                if (user == null) {
                    _uiState.value = ChangePasswordUiState.Error("Usuario no autenticado")
                    return@launch
                }
                
                // Reautenticar al usuario antes de cambiar la contraseña
                val email = user.email
                if (email == null) {
                    _uiState.value = ChangePasswordUiState.Error("No se pudo obtener el correo del usuario")
                    return@launch
                }
                
                // Reautenticar con la contraseña actual
                val credential = com.google.firebase.auth.EmailAuthProvider.getCredential(email, currentPassword)
                user.reauthenticate(credential).await()
                
                // Cambiar la contraseña
                user.updatePassword(newPassword).await()
                
                _uiState.value = ChangePasswordUiState.Success("Contraseña cambiada exitosamente")
                
            } catch (e: Exception) {
                val errorMessage = when {
                    e.message?.contains("wrong-password") == true -> "La contraseña actual es incorrecta"
                    e.message?.contains("weak-password") == true -> "La nueva contraseña es muy débil"
                    e.message?.contains("requires-recent-login") == true -> "Se requiere iniciar sesión recientemente para cambiar la contraseña"
                    else -> "Error al cambiar la contraseña: ${e.message}"
                }
                _uiState.value = ChangePasswordUiState.Error(errorMessage)
            }
        }
    }
    
    private fun validateInputs(currentPassword: String, newPassword: String, confirmPassword: String): Boolean {
        when {
            currentPassword.isBlank() -> {
                _uiState.value = ChangePasswordUiState.Error("Ingrese su contraseña actual")
                return false
            }
            newPassword.isBlank() -> {
                _uiState.value = ChangePasswordUiState.Error("Ingrese la nueva contraseña")
                return false
            }
            newPassword.length < 6 -> {
                _uiState.value = ChangePasswordUiState.Error("La nueva contraseña debe tener al menos 6 caracteres")
                return false
            }
            newPassword == currentPassword -> {
                _uiState.value = ChangePasswordUiState.Error("La nueva contraseña debe ser diferente a la actual")
                return false
            }
            confirmPassword.isBlank() -> {
                _uiState.value = ChangePasswordUiState.Error("Confirme la nueva contraseña")
                return false
            }
            newPassword != confirmPassword -> {
                _uiState.value = ChangePasswordUiState.Error("Las contraseñas no coinciden")
                return false
            }
        }
        return true
    }
    
    fun resetState() {
        _uiState.value = ChangePasswordUiState.Idle
    }
} 