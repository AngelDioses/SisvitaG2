package com.example.sisvitag2.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.delay


class LoginRepository(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {

    companion object {
        private const val TAG = "LoginRepository"
    }

    /**
     * Intenta iniciar sesión con correo electrónico y contraseña usando Firebase Authentication.
     *
     * @param email El correo electrónico del usuario.
     * @param password La contraseña del usuario.
     * @return Un objeto LoginResult que indica éxito o el tipo de error.
     */
    suspend fun login(email: String, password: String): LoginResult {
        // Ya no necesitamos withContext(Dispatchers.IO) explícitamente aquí,
        // porque las llamadas al SDK de Firebase Auth manejan su propia concurrencia
        // y .await() las hace suspendibles.

        // Validaciones básicas (opcional, pero recomendado)
        if (email.isBlank() || password.isBlank()) {
            Log.w(TAG, "Email o contraseña vacíos.")
            return LoginResult.Failure(LoginError.EMPTY_CREDENTIALS)
        }

        return try {
            Log.d(TAG, "Intentando iniciar sesión con email: $email")
            // Llama directamente a Firebase Authentication
            auth.signInWithEmailAndPassword(email, password).await()

            // Si await() no lanza excepción, el inicio de sesión fue exitoso
            val currentUser = auth.currentUser // Verifica que el usuario actual esté establecido
            if (currentUser != null) {
                Log.i(TAG, "Inicio de sesión exitoso para UID: ${currentUser.uid}")
                Log.d(TAG, "[DEBUG] isEmailVerified antes de reload: ${currentUser.isEmailVerified}")
                // Forzar actualización del estado de verificación del correo
                if (!currentUser.isEmailVerified) {
                    Log.d(TAG, "Correo no verificado, forzando actualización...")
                    try {
                        Log.d(TAG, "[DEBUG] Llamando a reload().await()...")
                        currentUser.reload().await()
                        Log.d(TAG, "[DEBUG] reload().await() completado")
                        // Pequeño delay para asegurar que Firebase se actualice
                        delay(1000)
                        Log.d(TAG, "[DEBUG] isEmailVerified después de reload: ${currentUser.isEmailVerified}")
                    } catch (e: Exception) {
                        Log.w(TAG, "Error al actualizar estado de verificación: ${e.message}")
                    }
                }
                Log.d(TAG, "[DEBUG] isEmailVerified FINAL antes de decisión: ${currentUser.isEmailVerified}")
                // Si el correo está verificado, permitir el login sin importar el estado en Firestore
                if (currentUser.isEmailVerified) {
                    Log.d(TAG, "Correo verificado, permitiendo login...")
                    return LoginResult.Success
                }
                
                // Si el correo no está verificado, validar estado en Firestore
                val userDoc = firestore.collection("usuarios").document(currentUser.uid).get().await()
                val estado = userDoc.getString("estado") ?: "pendiente"
                return when (estado) {
                    "aprobado" -> LoginResult.Success
                    "pendiente" -> LoginResult.Success // Permitir login pero mostrar pantalla de espera
                    "rechazado" -> LoginResult.Failure(LoginError.USER_DISABLED, "Tu cuenta ha sido rechazada.")
                    else -> LoginResult.Success // Por defecto permitir acceso
                }
            } else {
                Log.e(TAG, "Inicio de sesión pareció exitoso pero currentUser es null.")
                LoginResult.Failure(LoginError.UNKNOWN)
            }

        } catch (e: Exception) {
            // Manejar excepciones específicas de Firebase Auth
            Log.e(TAG, "Error durante el inicio de sesión", e)
            when (e) {
                is FirebaseAuthInvalidUserException -> {
                    // El correo electrónico no existe o está deshabilitado
                    LoginResult.Failure(LoginError.USER_NOT_FOUND)
                }
                is FirebaseAuthInvalidCredentialsException -> {
                    // La contraseña es incorrecta
                    LoginResult.Failure(LoginError.WRONG_PASSWORD)
                }
                // Puedes añadir más casos específicos si es necesario (ej. red)
                else -> {
                    // Otro tipo de error (problema de red, error del servidor Firebase, etc.)
                    LoginResult.Failure(LoginError.UNKNOWN, e.message)
                }
            }
        }
    }
}

// --- Clases auxiliares para manejar el resultado del login ---

sealed class LoginResult {
    object Success : LoginResult()
    data class Failure(val errorType: LoginError, val message: String? = null) : LoginResult()
}

enum class LoginError {
    USER_NOT_FOUND,
    WRONG_PASSWORD,
    EMPTY_CREDENTIALS,
    NETWORK_ERROR, // Podrías añadir detección específica de red si es necesario
    UNKNOWN,
    USER_DISABLED,
    INVALID_CREDENTIALS
}