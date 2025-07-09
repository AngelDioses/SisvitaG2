package com.example.sisvitag2.ui.vm // Ajusta el paquete

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

sealed class EmailVerificationState {
    object Idle : EmailVerificationState() // Estado inicial o después de un error no crítico
    object Sending : EmailVerificationState() // Enviando correo de verificación
    data class Sent(val email: String) : EmailVerificationState() // Correo enviado, esperando verificación
    object Verified : EmailVerificationState() // Correo verificado
    data class Error(val message: String) : EmailVerificationState()
    object Checking : EmailVerificationState() // Comprobando estado de verificación
}

class EmailVerificationViewModel(
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _verificationState = MutableStateFlow<EmailVerificationState>(EmailVerificationState.Idle)
    val verificationState: StateFlow<EmailVerificationState> = _verificationState.asStateFlow()

    private var pollingJob: Job? = null
    private var currentUserForVerification: FirebaseUser? = null

    companion object {
        private const val TAG = "EmailVerificationVM"
    }

    fun sendVerificationEmail() {
        val user = auth.currentUser
        if (user == null) {
            _verificationState.value = EmailVerificationState.Error("No hay usuario autenticado para verificar.")
            return
        }
        if (user.isEmailVerified) {
            _verificationState.value = EmailVerificationState.Verified
            return
        }

        _verificationState.value = EmailVerificationState.Sending
        user.sendEmailVerification()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Correo de verificación enviado a ${user.email}")
                    currentUserForVerification = user // Guardar usuario para polling
                    _verificationState.value = EmailVerificationState.Sent(user.email ?: "tu correo")
                    // Iniciar polling opcionalmente o esperar a que el usuario reintente/actualice
                    // startPollingEmailVerificationStatus() // Descomentar si quieres polling automático
                } else {
                    Log.e(TAG, "Error al enviar correo de verificación", task.exception)
                    _verificationState.value = EmailVerificationState.Error(
                        task.exception?.message ?: "No se pudo enviar el correo de verificación."
                    )
                }
            }
    }

    fun checkEmailVerificationStatus() {
        val user = currentUserForVerification ?: auth.currentUser // Usar el guardado o el actual
        if (user == null) {
            _verificationState.value = EmailVerificationState.Error("No hay usuario para comprobar.")
            return
        }

        _verificationState.value = EmailVerificationState.Checking
        // Recargar el estado del usuario desde Firebase para obtener la información más reciente
        user.reload().addOnCompleteListener { reloadTask ->
            Log.d(TAG, "[DEBUG] Llamando a reload() en checkEmailVerificationStatus...")
            if (reloadTask.isSuccessful) {
                Log.d(TAG, "[DEBUG] reload() completado. isEmailVerified: ${user.isEmailVerified}")
                if (user.isEmailVerified) {
                    Log.d(TAG, "Correo verificado para ${user.email}")
                    _verificationState.value = EmailVerificationState.Verified
                    stopPollingEmailVerificationStatus()
                } else {
                    Log.d(TAG, "Correo aún no verificado para ${user.email}")
                    // Volver al estado Sent si no está verificado y estaba en Checking
                    if (_verificationState.value is EmailVerificationState.Checking) {
                        _verificationState.value = EmailVerificationState.Sent(user.email ?: "tu correo")
                    }
                }
            } else {
                Log.e(TAG, "Error al recargar el estado del usuario.", reloadTask.exception)
                _verificationState.value = EmailVerificationState.Error(
                    reloadTask.exception?.message ?: "No se pudo comprobar el estado de verificación."
                )
            }
        }
    }

    // Opcional: Polling automático para comprobar la verificación
    private fun startPollingEmailVerificationStatus() {
        pollingJob?.cancel() // Cancela cualquier polling anterior
        pollingJob = viewModelScope.launch {
            while (isActive) { // Mientras el ViewModel esté activo
                delay(5000) // Espera 5 segundos
                val user = currentUserForVerification ?: auth.currentUser
                if (user == null || user.isEmailVerified) {
                    if (user?.isEmailVerified == true) {
                        _verificationState.value = EmailVerificationState.Verified
                    }
                    stopPollingEmailVerificationStatus() // Detener si ya no hay usuario o está verificado
                    break
                }
                // Recargar y comprobar
                user.reload().addOnCompleteListener { task ->
                    Log.d(TAG, "[DEBUG] Llamando a reload() en polling...")
                    if (task.isSuccessful && user.isEmailVerified) {
                        Log.d(TAG, "[DEBUG] reload() completado en polling. isEmailVerified: ${user.isEmailVerified}")
                        _verificationState.value = EmailVerificationState.Verified
                        stopPollingEmailVerificationStatus()
                    } else if (!task.isSuccessful) {
                        Log.w(TAG, "Polling: Error al recargar usuario", task.exception)
                        // Podrías detener el polling o reintentar con backoff
                    } else {
                        Log.d(TAG, "Polling: Correo aún no verificado para ${user.email}")
                    }
                }
            }
        }
        Log.d(TAG, "Polling iniciado para verificación de correo.")
    }

    private fun stopPollingEmailVerificationStatus() {
        pollingJob?.cancel()
        pollingJob = null
        Log.d(TAG, "Polling detenido.")
    }

    fun resetState() {
        _verificationState.value = EmailVerificationState.Idle
        currentUserForVerification = null
        stopPollingEmailVerificationStatus()
    }

    override fun onCleared() {
        super.onCleared()
        stopPollingEmailVerificationStatus() // Asegurar que se detiene el polling
    }
}