package com.example.sisvitag2.ui.vm // Ajusta el paquete si es necesario

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class AuthState {
    object Loading : AuthState()
    data class Authenticated(val user: FirebaseUser) : AuthState()
    object Unauthenticated : AuthState()
}

class SessionViewModel(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _userName = MutableStateFlow<String?>(null)
    val userName: StateFlow<String?> = _userName.asStateFlow()

    private val _userId = MutableStateFlow<String?>(null)
    val userId: StateFlow<String?> = _userId.asStateFlow()

    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        val user = firebaseAuth.currentUser
        viewModelScope.launch {
            if (user != null) {
                _authState.value = AuthState.Authenticated(user)
                _userId.value = user.uid
                fetchUserNameData(user)
            } else {
                _authState.value = AuthState.Unauthenticated
                _userName.value = null
                _userId.value = null
            }
            Log.d("SessionViewModel", "AuthState updated: ${_authState.value}")
        }
    }

    init {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            _authState.value = AuthState.Authenticated(currentUser)
            _userId.value = currentUser.uid
            fetchUserNameData(currentUser)
        } else {
            _authState.value = AuthState.Unauthenticated
        }
        auth.addAuthStateListener(authStateListener)
        Log.d("SessionViewModel", "SessionViewModel inicializado. Current user: ${currentUser?.uid}")
    }

    private fun fetchUserNameData(user: FirebaseUser) {
        if (!user.displayName.isNullOrBlank()) {
            _userName.value = user.displayName
            Log.d("SessionViewModel", "Nombre obtenido de Auth displayName: ${user.displayName}")
            return
        }
        Log.d("SessionViewModel", "displayName nulo o vacío. Intentando obtener nombre de Firestore para UID: ${user.uid}")
        viewModelScope.launch {
            try {
                val personDoc = firestore.collection("personas").document(user.uid).get().await()
                if (personDoc.exists()) {
                    val nombre = personDoc.getString("nombre") ?: ""
                    val apellido = personDoc.getString("apellidopaterno") ?: ""
                    val fetchedName = "$nombre $apellido".trim()
                    _userName.value = if (fetchedName.isNotBlank()) fetchedName else "Usuario"
                    Log.i("SessionViewModel", "Nombre obtenido de Firestore 'personas' para ${user.uid}: $fetchedName")
                } else {
                    _userName.value = "Usuario (Sin perfil)"
                    Log.w("SessionViewModel", "No se encontró documento en 'personas' para UID: ${user.uid}")
                }
            } catch (e: Exception) {
                Log.e("SessionViewModel", "Error al obtener nombre de Firestore para ${user.uid}", e)
                _userName.value = "Usuario (Error DB)"
            }
        }
    }

    fun signOut() {
        Log.d("SessionViewModel", "Intentando cerrar sesión...")
        auth.signOut()
    }

    override fun onCleared() {
        super.onCleared()
        auth.removeAuthStateListener(authStateListener)
        Log.d("SessionViewModel", "SessionViewModel limpiado y listener de Auth removido.")
    }
}