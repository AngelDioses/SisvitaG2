package com.example.sisvitag2.ui.vm

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sisvitag2.data.model.UserProfileData
import com.example.sisvitag2.data.repository.AccountRepository
import com.google.firebase.auth.FirebaseAuth // Necesario para obtener el UID actual
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Ampliamos AccountUiState para manejar el estado de actualización
sealed class AccountUiState {
    object Idle : AccountUiState() // Estado inicial o después de una operación
    object LoadingProfile : AccountUiState() // Cargando perfil inicial
    data class ProfileLoaded(val userProfile: UserProfileData) : AccountUiState() // Perfil cargado
    object UpdatingProfile : AccountUiState() // Actualizando datos o foto
    data class UpdateSuccess(val message: String, val updatedProfile: UserProfileData? = null) : AccountUiState() // Éxito al actualizar
    data class Error(val message: String) : AccountUiState()
}

class AccountViewModel(
    private val accountRepository: AccountRepository,
    private val auth: FirebaseAuth, // Inyectar FirebaseAuth para obtener el UID
    private val sessionViewModel: SessionViewModel // Inyectar SessionViewModel directamente
) : ViewModel() {
    
    // Callback para notificar cambios al SessionViewModel
    var onProfileUpdated: (() -> Unit)? = null
    
    // Eliminamos la referencia manual ya que ahora viene por inyección
    // var sessionViewModel: SessionViewModel? = null

    private val _uiState = MutableStateFlow<AccountUiState>(AccountUiState.Idle)
    val uiState: StateFlow<AccountUiState> = _uiState.asStateFlow()

    companion object {
        private const val TAG = "AccountViewModel"
    }

    init {
        Log.d(TAG, "=== ACCOUNTVIEWMODEL INICIALIZADO ===")
        loadUserProfile()
    }

    fun loadUserProfile() {
        if (_uiState.value is AccountUiState.LoadingProfile || _uiState.value is AccountUiState.UpdatingProfile) return

        Log.d(TAG, "Iniciando carga de perfil de usuario.")
        _uiState.value = AccountUiState.LoadingProfile
        viewModelScope.launch {
            val result = accountRepository.getUserProfile()
            result.fold(
                onSuccess = { userProfile ->
                    Log.i(TAG, "Perfil de usuario cargado: ${userProfile.displayName}")
                    // Tomar estado y rol del SessionViewModel
                    val estado = sessionViewModel.userEstado.value
                    val rol = sessionViewModel.userRol.value
                    val userProfileWithEstado = userProfile.copy(estado = estado, legacyTipoUsuarioId = rol)
                    _uiState.value = AccountUiState.ProfileLoaded(userProfileWithEstado)
                },
                onFailure = { exception ->
                    Log.e(TAG, "Error al cargar perfil de usuario.", exception)
                    _uiState.value = AccountUiState.Error(exception.message ?: "No se pudo cargar el perfil.")
                }
            )
        }
    }

    fun updateUserPersonalData(dataToUpdate: Map<String, Any>) {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null) {
            _uiState.value = AccountUiState.Error("Usuario no autenticado para actualizar perfil.")
            return
        }
        if (dataToUpdate.isEmpty()) {
            return
        }

        Log.d(TAG, "Iniciando actualización de datos personales para usuario: $currentUserId")
        Log.d(TAG, "Datos a actualizar: $dataToUpdate")
        
        _uiState.value = AccountUiState.UpdatingProfile
        viewModelScope.launch {
            try {
                Log.d(TAG, "Llamando a accountRepository.updateUserPersonalData...")
                val result = accountRepository.updateUserPersonalData(currentUserId, dataToUpdate)
                Log.d(TAG, "Resultado de updateUserPersonalData: $result")
                
                result.fold(
                    onSuccess = {
                        Log.i(TAG, "Datos personales actualizados exitosamente.")
                        // Recargar el perfil para reflejar los cambios
                        Log.d(TAG, "Recargando perfil después de actualización...")
                        reloadProfileAfterUpdate()
                    },
                    onFailure = { exception ->
                        Log.e(TAG, "Error actualizando datos personales.", exception)
                        _uiState.value = AccountUiState.Error(exception.message ?: "No se pudieron guardar los cambios.")
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Excepción inesperada durante actualización", e)
                _uiState.value = AccountUiState.Error("Error inesperado: ${e.message}")
            }
        }
    }
    
    private fun reloadProfileAfterUpdate() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Recargando perfil...")
                val result = accountRepository.getUserProfile()
                result.fold(
                    onSuccess = { userProfile ->
                        Log.i(TAG, "Perfil recargado exitosamente después de actualización: ${userProfile.displayName}")
                        _uiState.value = AccountUiState.UpdateSuccess("Perfil actualizado con éxito.", userProfile)
                        // Notificar al SessionViewModel que el perfil se actualizó
                        Log.d(TAG, "Invocando callback onProfileUpdated...")
                        onProfileUpdated?.invoke()
                        Log.d(TAG, "Callback onProfileUpdated ejecutado.")
                        
                        // Forzar actualización del SessionViewModel usando el nuevo método
                        Log.d(TAG, "Forzando actualización del SessionViewModel...")
                        sessionViewModel.forceUpdateUserName()
                        
                        // Esperar un poco y forzar otra actualización para asegurar que se actualice
                        kotlinx.coroutines.delay(500)
                        sessionViewModel.forceUpdateUserName()
                        Log.d(TAG, "SessionViewModel actualizado completamente.")
                    },
                    onFailure = { exception ->
                        Log.e(TAG, "Error recargando perfil después de actualización", exception)
                        _uiState.value = AccountUiState.Error("Perfil actualizado pero error al recargar: ${exception.message}")
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Excepción recargando perfil", e)
                _uiState.value = AccountUiState.Error("Error recargando perfil: ${e.message}")
            }
        }
    }

    fun updateUserProfilePicture(imageUri: Uri) {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null) {
            _uiState.value = AccountUiState.Error("Usuario no autenticado para cambiar foto.")
            return
        }

        _uiState.value = AccountUiState.UpdatingProfile
        viewModelScope.launch {
            try {
                val result = accountRepository.updateUserProfilePicture(currentUserId, imageUri)
                result.fold(
                    onSuccess = { downloadUrl ->
                        Log.i(TAG, "Foto de perfil actualizada. Nueva URL: $downloadUrl")
                        // Volver a cargar el perfil para reflejar el cambio de foto
                        reloadProfileAfterUpdate()
                    },
                    onFailure = { exception ->
                        Log.e(TAG, "Error actualizando foto de perfil.", exception)
                        _uiState.value = AccountUiState.Error(exception.message ?: "No se pudo cambiar la foto.")
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Excepción inesperada durante actualización de foto", e)
                _uiState.value = AccountUiState.Error("Error inesperado: ${e.message}")
            }
        }
    }

    // Llama a esta función desde la UI para volver al estado ProfileLoaded después de mostrar un mensaje de UpdateSuccess
    fun acknowledgeUpdate() {
        if (_uiState.value is AccountUiState.UpdateSuccess) {
            val currentProfile = (_uiState.value as AccountUiState.UpdateSuccess).updatedProfile
            if (currentProfile != null) {
                _uiState.value = AccountUiState.ProfileLoaded(currentProfile)
            } else {
                // Si por alguna razón no tenemos el perfil actualizado, recargamos
                loadUserProfile()
            }
        } else if (_uiState.value is AccountUiState.Error) {
            // Si hubo un error y el usuario lo descarta, podríamos recargar el perfil
            loadUserProfile()
        }
    }
}