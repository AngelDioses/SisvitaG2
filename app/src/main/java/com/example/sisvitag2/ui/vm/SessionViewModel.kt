package com.example.sisvitag2.ui.vm // Ajusta el paquete si es necesario

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
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
        // Primero intenta obtener el displayName de Firebase Auth, que debería
        // haberse actualizado durante el registro si esa operación tuvo éxito.
        if (!user.displayName.isNullOrBlank()) {
            _userName.value = user.displayName
            Log.d("SessionViewModel", "Nombre obtenido de Auth displayName: ${user.displayName}")
            return
        }

        // Si displayName es nulo o vacío, intenta leer de Firestore desde la colección "usuarios"
        Log.d("SessionViewModel", "displayName nulo/vacío en Auth. Intentando obtener nombre de Firestore 'usuarios' para UID: ${user.uid}")
        viewModelScope.launch {
            try {
                // ***** MODIFICACIÓN AQUÍ *****
                val userDocument = firestore.collection("usuarios").document(user.uid).get().await()
                // ***** FIN DE MODIFICACIÓN *****

                if (userDocument.exists()) {
                    val nombre = userDocument.getString("nombre") ?: ""
                    val apellido = userDocument.getString("apellidopaterno") ?: "" // Usa el nombre exacto del campo en Firestore
                    val fetchedName = "$nombre $apellido".trim()
                    _userName.value = if (fetchedName.isNotBlank()) fetchedName else "Usuario"
                    Log.i("SessionViewModel", "Nombre obtenido de Firestore 'usuarios' para ${user.uid}: $fetchedName")

                    // Opcional: Si el displayName en Auth estaba vacío pero lo encontramos en Firestore,
                    // podríamos intentar actualizarlo en Auth para futuras sesiones.
                    if (user.displayName.isNullOrBlank() && fetchedName.isNotBlank()) {
                        try {
                            val profileUpdates = UserProfileChangeRequest.Builder()
                                .setDisplayName(fetchedName)
                                .build()
                            user.updateProfile(profileUpdates).await()
                            Log.i("SessionViewModel", "displayName en Firebase Auth actualizado a: $fetchedName")
                        } catch (e: Exception) {
                            Log.w("SessionViewModel", "No se pudo actualizar displayName en Auth desde Firestore.", e)
                        }
                    }

                } else {
                    _userName.value = "Usuario (Perfil no encontrado en DB)"
                    Log.w("SessionViewModel", "No se encontró documento en 'usuarios' para UID: ${user.uid}")
                }
            } catch (e: Exception) {
                Log.e("SessionViewModel", "Error al obtener nombre de Firestore para ${user.uid}", e)
                _userName.value = "Usuario (Error al cargar)"
            }
        }
    }

    fun signOut() {
        Log.d("SessionViewModel", "Intentando cerrar sesión...")
        auth.signOut()
        // El listener se encargará de actualizar _authState, y por ende _userName y _userId
    }

    override fun onCleared() {
        super.onCleared()
        auth.removeAuthStateListener(authStateListener)
        Log.d("SessionViewModel", "SessionViewModel limpiado y listener de Auth removido.")
    }
}