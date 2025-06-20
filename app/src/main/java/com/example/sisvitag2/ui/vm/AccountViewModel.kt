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
    private val auth: FirebaseAuth // Inyectar FirebaseAuth para obtener el UID
) : ViewModel() {

    private val _uiState = MutableStateFlow<AccountUiState>(AccountUiState.Idle)
    val uiState: StateFlow<AccountUiState> = _uiState.asStateFlow()

    companion object {
        private const val TAG = "AccountViewModel"
    }

    init {
        Log.d(TAG, "AccountViewModel inicializado.")
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
                    _uiState.value = AccountUiState.ProfileLoaded(userProfile)
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
            // Opcional: puedes emitir un mensaje de éxito si no hay nada que cambiar o simplemente no hacer nada.
            // _uiState.value = AccountUiState.UpdateSuccess("No hay cambios para guardar.")
            return
        }

        _uiState.value = AccountUiState.UpdatingProfile
        viewModelScope.launch {
            val result = accountRepository.updateUserPersonalData(currentUserId, dataToUpdate)
            result.fold(
                onSuccess = {
                    Log.i(TAG, "Datos personales actualizados exitosamente.")
                    // Volver a cargar el perfil para reflejar los cambios
                    val updatedProfileResult = accountRepository.getUserProfile()
                    updatedProfileResult.fold(
                        onSuccess = { newProfile ->
                            _uiState.value = AccountUiState.UpdateSuccess("Perfil actualizado con éxito.", newProfile)
                        },
                        onFailure = {
                            _uiState.value = AccountUiState.UpdateSuccess("Perfil actualizado, pero error al recargar.")
                        }
                    )
                },
                onFailure = { exception ->
                    Log.e(TAG, "Error actualizando datos personales.", exception)
                    _uiState.value = AccountUiState.Error(exception.message ?: "No se pudieron guardar los cambios.")
                    // Recargar el perfil original si la actualización falla para revertir la UI al estado anterior
                    loadUserProfile()
                }
            )
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
            val result = accountRepository.updateUserProfilePicture(currentUserId, imageUri)
            result.fold(
                onSuccess = { downloadUrl ->
                    Log.i(TAG, "Foto de perfil actualizada. Nueva URL: $downloadUrl")
                    // Volver a cargar el perfil para reflejar el cambio de foto
                    val updatedProfileResult = accountRepository.getUserProfile()
                    updatedProfileResult.fold(
                        onSuccess = { newProfile ->
                            _uiState.value = AccountUiState.UpdateSuccess("Foto de perfil actualizada.", newProfile)
                        },
                        onFailure = {
                            _uiState.value = AccountUiState.UpdateSuccess("Foto actualizada, pero error al recargar perfil.")
                        }
                    )
                },
                onFailure = { exception ->
                    Log.e(TAG, "Error actualizando foto de perfil.", exception)
                    _uiState.value = AccountUiState.Error(exception.message ?: "No se pudo cambiar la foto.")
                    loadUserProfile() // Reintentar cargar el perfil original
                }
            )
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