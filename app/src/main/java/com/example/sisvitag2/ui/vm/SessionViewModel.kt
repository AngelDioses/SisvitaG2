package com.example.sisvitag2.ui.vm // Ajusta el paquete si es necesario

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.CancellationException

sealed class AuthState {
    object Loading : AuthState()
    data class Authenticated(val user: FirebaseUser) : AuthState()
    object Unauthenticated : AuthState()
}

class SessionViewModel(
    val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _authState = mutableStateOf<AuthState>(AuthState.Loading)
    val authState: State<AuthState> = _authState

    private val _userName = mutableStateOf<String?>(null)
    val userName: State<String?> = _userName

    private val _userPhotoUrl = mutableStateOf<String?>(null)
    val userPhotoUrl: State<String?> = _userPhotoUrl

    private val _userId = mutableStateOf<String?>(null)
    val userId: State<String?> = _userId

    private val _userEstado = mutableStateOf<String?>(null)
    val userEstado: State<String?> = _userEstado

    private val _userRol = mutableStateOf<Int?>(null)
    val userRol: State<Int?> = _userRol

    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        val user = firebaseAuth.currentUser
        viewModelScope.launch {
            if (user != null) {
                try {
                    Log.d("SessionViewModel", "[DEBUG] Llamando a reload() antes de emitir AuthState.Authenticated...")
                    user.reload().await()
                    Log.d("SessionViewModel", "[DEBUG] reload() completado. isEmailVerified: ${user.isEmailVerified}")
                } catch (e: Exception) {
                    Log.w("SessionViewModel", "[DEBUG] Error en reload(): ${e.message}")
                }
                Log.d("SessionViewModel", "[DEBUG] isEmailVerified FINAL antes de AuthState: ${user.isEmailVerified}")
                _authState.value = AuthState.Authenticated(user)
                _userId.value = user.uid
                fetchUserNameData(user)
            } else {
                _authState.value = AuthState.Unauthenticated
                _userName.value = null
                _userPhotoUrl.value = null
                _userId.value = null
            }
            Log.d("SessionViewModel", "AuthState updated: ${_authState.value}")
        }
    }

    init {
        Log.d("SessionViewModel", "=== SESSIONVIEWMODEL INICIALIZADO ===")
        val currentUser = auth.currentUser
        if (currentUser != null) {
            _authState.value = AuthState.Authenticated(currentUser)
            _userId.value = currentUser.uid
            fetchUserNameData(currentUser)
        } else {
            _authState.value = AuthState.Unauthenticated
            _userName.value = null
            _userPhotoUrl.value = null
            _userId.value = null
        }
        auth.addAuthStateListener(authStateListener)
        Log.d("SessionViewModel", "SessionViewModel inicializado. Current user: ${currentUser?.uid}")
    }

    fun fetchUserNameData(user: FirebaseUser?) {
        Log.d("SessionViewModel", "fetchUserNameData llamado con usuario: ${user?.uid}")
        if (user == null) {
            Log.w("SessionViewModel", "Usuario es null, no se puede actualizar nombre")
            return
        }
        
        // Siempre intentar leer de Firestore primero para obtener los datos más actualizados
        Log.d("SessionViewModel", "Obteniendo nombre de Firestore 'usuarios' para UID: ${user.uid}")
        viewModelScope.launch {
            try {
                val userDocument = firestore.collection("usuarios").document(user.uid).get().await()

                // Log para ver el contenido completo del documento Firestore
                Log.d("SessionViewModel", "Snapshot Firestore: ${userDocument.data}")

                if (userDocument.exists()) {
                    val nombre = userDocument.getString("nombre") ?: ""
                    val apellido = userDocument.getString("apellidopaterno") ?: ""
                    val photoUrl = userDocument.getString("photoUrl") // Obtener URL de la foto
                    val estado = userDocument.getString("estado")
                    val rol = userDocument.getLong("legacyTipoUsuarioId")?.toInt()
                    val fetchedName = "$nombre $apellido".trim()
                    val oldName = _userName.value
                    val newName = if (fetchedName.isNotBlank()) fetchedName else "Usuario"
                    
                    Log.i("SessionViewModel", "Nombre obtenido de Firestore 'usuarios' para ${user.uid}: $fetchedName")
                    Log.d("SessionViewModel", "userName actualizado de '$oldName' a '$newName'")
                    
                    // Actualizar el MutableState para el nombre
                    _userName.value = newName
                    
                    // Actualizar el MutableState para la foto
                    _userPhotoUrl.value = photoUrl
                    
                    // Actualizar el MutableState para el estado
                    val oldEstado = _userEstado.value
                    _userEstado.value = estado
                    // Actualizar el MutableState para el rol
                    val oldRol = _userRol.value
                    _userRol.value = rol
                    
                    Log.d("SessionViewModel", "Estado actualizado de '$oldEstado' a '$estado'")
                    Log.d("SessionViewModel", "Rol actualizado de $oldRol a $rol")
                    
                    // Si el valor es el mismo, forzar una emisión adicional
                    if (oldName == newName) {
                        Log.d("SessionViewModel", "Valor igual, forzando emisión adicional...")
                        _userName.value = newName
                    }

                    // Actualizar también el displayName en Firebase Auth para consistencia
                    if (fetchedName.isNotBlank() && user.displayName != fetchedName) {
                        try {
                            val profileUpdates = UserProfileChangeRequest.Builder()
                                .setDisplayName(fetchedName)
                                .build()
                            user.updateProfile(profileUpdates).await()
                            Log.i("SessionViewModel", "displayName en Firebase Auth actualizado a: $fetchedName")
                        } catch (e: Exception) {
                            Log.w("SessionViewModel", "No se pudo actualizar displayName en Auth", e)
                        }
                    }
                } else {
                    Log.w("SessionViewModel", "No se encontró documento en 'usuarios' para UID: ${user.uid}")
                    // Si no existe documento en Firestore, usar el displayName de Auth
                    val oldName = _userName.value
                    val newName = user.displayName ?: "Usuario"
                    Log.i("SessionViewModel", "Usuario autenticado sin documento en Firestore, usando displayName: $newName")
                    Log.d("SessionViewModel", "userName actualizado de '$oldName' a '$newName'")
                    
                    // Actualizar el MutableState para el nombre
                    _userName.value = newName
                    
                    // Limpiar la URL de la foto si no hay documento
                    _userPhotoUrl.value = null
                    
                    // Limpiar el estado y el rol si no hay documento
                    val oldEstado = _userEstado.value
                    val oldRol = _userRol.value
                    _userEstado.value = null
                    _userRol.value = null
                    
                    Log.d("SessionViewModel", "Estado limpiado de '$oldEstado' a null (no hay documento)")
                    Log.d("SessionViewModel", "Rol limpiado de $oldRol a null (no hay documento)")
                    
                    // Si el valor es el mismo, forzar una emisión adicional
                    if (oldName == newName) {
                        Log.d("SessionViewModel", "Valor igual, forzando emisión adicional...")
                        _userName.value = newName
                    }
                }
            } catch (e: CancellationException) {
                Log.d("SessionViewModel", "Corrutina cancelada para usuario ${user.uid}: ${e.message}")
                // No hacer nada si la corrutina fue cancelada
            } catch (e: Exception) {
                Log.e("SessionViewModel", "Error al obtener nombre de Firestore para ${user.uid}: ${e.message}", e)
                // En caso de error, usar el displayName de Auth como fallback
                val oldName = _userName.value
                val newName = user.displayName ?: "Usuario (Error al cargar)"
                Log.d("SessionViewModel", "userName actualizado de '$oldName' a '$newName' (fallback por error)")
                
                // Actualizar el MutableState para el nombre
                _userName.value = newName
                
                // Limpiar la URL de la foto en caso de error
                _userPhotoUrl.value = null
                
                // Limpiar el estado y el rol en caso de error
                val oldEstado = _userEstado.value
                val oldRol = _userRol.value
                _userEstado.value = null
                _userRol.value = null
                
                Log.d("SessionViewModel", "Estado limpiado de '$oldEstado' a null (error)")
                Log.d("SessionViewModel", "Rol limpiado de $oldRol a null (error)")
                
                // Si el valor es el mismo, forzar una emisión adicional
                if (oldName == newName) {
                    Log.d("SessionViewModel", "Valor igual, forzando emisión adicional...")
                    _userName.value = newName
                }
            }
        }
        Log.d("SessionViewModel", "fetchUserNameData completado. Nombre actual: ${_userName.value}")
    }

    fun forceUpdateUserName() {
        Log.d("SessionViewModel", "forceUpdateUserName llamado")
        val currentUser = auth.currentUser
        if (currentUser != null) {
            fetchUserNameData(currentUser)
        } else {
            Log.w("SessionViewModel", "No hay usuario autenticado para forzar actualización")
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