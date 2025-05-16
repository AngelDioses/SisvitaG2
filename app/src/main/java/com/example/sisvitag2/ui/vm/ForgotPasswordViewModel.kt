package com.example.sisvitag2.ui.vm

import android.util.Log
import androidx.core.util.PatternsCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ForgotPasswordState {
    object Idle : ForgotPasswordState()
    object Loading : ForgotPasswordState()
    data class Success(val message: String) : ForgotPasswordState()
    data class Error(val message: String) : ForgotPasswordState()
}

class ForgotPasswordViewModel(
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _forgotPasswordState = MutableStateFlow<ForgotPasswordState>(ForgotPasswordState.Idle)
    val forgotPasswordState: StateFlow<ForgotPasswordState> = _forgotPasswordState.asStateFlow()

    companion object {
        private const val TAG = "ForgotPasswordVM"
    }

    fun sendPasswordResetEmail(email: String) {
        if (email.isBlank() || !PatternsCompat.EMAIL_ADDRESS.matcher(email).matches()) {
            _forgotPasswordState.value = ForgotPasswordState.Error("Por favor, ingrese un correo electrónico válido.")
            return
        }

        _forgotPasswordState.value = ForgotPasswordState.Loading
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Correo de restablecimiento de contraseña enviado a $email")
                    _forgotPasswordState.value = ForgotPasswordState.Success(
                        "Si $email está registrado, se ha enviado un enlace para restablecer tu contraseña."
                    )
                } else {
                    Log.w(TAG, "Error al enviar correo de restablecimiento.", task.exception)
                    // Firebase no siempre dice si el correo no existe por seguridad.
                    _forgotPasswordState.value = ForgotPasswordState.Error(
                        task.exception?.message ?: "No se pudo enviar el correo. Inténtalo de nuevo."
                    )
                }
            }
    }

    fun resetState() {
        _forgotPasswordState.value = ForgotPasswordState.Idle
    }
}