package com.example.sisvitag2.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sisvitag2.data.model.UserProfileData
import com.example.sisvitag2.data.repository.AccountRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AdminUiState {
    object Idle : AdminUiState()
    object Loading : AdminUiState()
    data class UsersLoaded(val users: List<UserProfileData>) : AdminUiState()
    data class Success(val message: String) : AdminUiState()
    data class Error(val message: String) : AdminUiState()
}

class AdminViewModel(
    private val accountRepository: AccountRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<AdminUiState>(AdminUiState.Idle)
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    fun loadUsers() {
        _uiState.value = AdminUiState.Loading
        viewModelScope.launch {
            val result = accountRepository.getAllUsers()
            result.fold(
                onSuccess = { users -> _uiState.value = AdminUiState.UsersLoaded(users) },
                onFailure = { e -> _uiState.value = AdminUiState.Error(e.message ?: "Error al cargar usuarios") }
            )
        }
    }

    fun updateUserRoleAndStatus(userId: String, newRole: Int, newStatus: String) {
        _uiState.value = AdminUiState.Loading
        viewModelScope.launch {
            val result = accountRepository.updateUserRoleAndStatus(userId, newRole, newStatus)
            result.fold(
                onSuccess = {
                    _uiState.value = AdminUiState.Success("Rol y estado actualizados correctamente.")
                    loadUsers()
                },
                onFailure = { e -> _uiState.value = AdminUiState.Error(e.message ?: "Error al actualizar rol/estado") }
            )
        }
    }

    fun deleteUser(userId: String) {
        _uiState.value = AdminUiState.Loading
        viewModelScope.launch {
            val result = accountRepository.deleteUserCompletely(userId)
            result.fold(
                onSuccess = {
                    _uiState.value = AdminUiState.Success("Usuario eliminado correctamente.")
                    loadUsers()
                },
                onFailure = { e -> _uiState.value = AdminUiState.Error(e.message ?: "Error al eliminar usuario") }
            )
        }
    }

    fun resetState() {
        _uiState.value = AdminUiState.Idle
    }
} 