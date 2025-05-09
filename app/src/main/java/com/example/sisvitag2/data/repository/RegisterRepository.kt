package com.example.sisvitag2.data.repository

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale

// Data class para la colección 'personas'
data class FirestorePerson(
    val nombre: String,
    val apellidopaterno: String,
    val apellidomaterno: String?,
    val fechanacimiento: Timestamp,
    val ubigeoid: String,
    val tipo_documento: String,
    val numero_documento: String,
    val genero: String,
    val telefono: String?
)

// Data class para la colección 'usuarios'
data class FirestoreUser(
    val persona_id: String,
    val correo: String,
    val tipousuarioid: String
)

class RegisterRepository(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {

    companion object {
        private const val TAG = "RegisterRepository"
        private const val USERS_COLLECTION = "usuarios"
        private const val PERSONAS_COLLECTION = "personas"
        private const val UBIGEOS_COLLECTION = "ubigeos"
        private const val TIPOS_USUARIO_COLLECTION = "tipos_usuario"
        private const val TIPOS_DOCUMENTO_FIRESTORE_COLLECTION = "tipos_documento"
        private const val GENEROS_FIRESTORE_COLLECTION = "generos"
    }

    suspend fun register(
        email: String,
        password: String,
        profileDetails: Map<String, Any>
    ): RegisterResult {
        if (email.isBlank() || password.isBlank()) {
            return RegisterResult.Failure(RegisterError.EMPTY_CREDENTIALS, "Email o contraseña vacíos.")
        }

        try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user
                ?: return RegisterResult.Failure(RegisterError.UNKNOWN, "Error inesperado, usuario de Auth es null.")
            Log.i(TAG, "Usuario creado en Auth con UID: ${firebaseUser.uid}")

            val nombreCompleto = "${profileDetails["nombre"] as? String ?: ""} ${profileDetails["apellidopaterno"] as? String ?: ""}".trim()
            if (nombreCompleto.isNotBlank()) {
                val profileUpdates = UserProfileChangeRequest.Builder().setDisplayName(nombreCompleto).build()
                try {
                    firebaseUser.updateProfile(profileUpdates).await()
                    Log.i(TAG, "Firebase Auth displayName actualizado a: $nombreCompleto")
                } catch (e: Exception) {
                    Log.w(TAG, "Error actualizando displayName en Firebase Auth", e)
                }
            }

            val departamento = profileDetails["departamento"] as? String ?: ""
            val provincia = profileDetails["provincia"] as? String ?: ""
            val distrito = profileDetails["distrito"] as? String ?: ""
            val ubigeoId = findUbigeoId(departamento, provincia, distrito)
                ?: return RegisterResult.Failure(RegisterError.FIRESTORE_ERROR, "No se pudo encontrar el ubigeo. Verifica los datos.")

            val tipoUsuarioDesc = profileDetails["role_description"] as? String ?: "Paciente"
            val tipoUsuarioId = findTipoUsuarioId(tipoUsuarioDesc)
                ?: return RegisterResult.Failure(RegisterError.FIRESTORE_ERROR, "Tipo de usuario no válido: $tipoUsuarioDesc")

            val birthDateString = profileDetails["fechanacimiento_str"] as? String
            val birthDateTimestamp = birthDateString?.let { parseDateToTimestamp(it) }
                ?: return RegisterResult.Failure(RegisterError.INVALID_DATA, "Fecha de nacimiento inválida o faltante.")

            val personaDocId = firebaseUser.uid
            val personaData = FirestorePerson(
                nombre = profileDetails["nombre"] as? String ?: "",
                apellidopaterno = profileDetails["apellidopaterno"] as? String ?: "",
                apellidomaterno = profileDetails["apellidomaterno"] as? String,
                fechanacimiento = birthDateTimestamp,
                ubigeoid = ubigeoId,
                tipo_documento = profileDetails["tipo_documento"] as? String ?: "",
                numero_documento = profileDetails["numero_documento"] as? String ?: "",
                genero = profileDetails["genero"] as? String ?: "",
                telefono = profileDetails["telefono"] as? String
            )
            firestore.collection(PERSONAS_COLLECTION).document(personaDocId).set(personaData).await()
            Log.i(TAG, "Datos guardados en $PERSONAS_COLLECTION.")

            val usuarioData = FirestoreUser(
                persona_id = personaDocId,
                correo = firebaseUser.email!!,
                tipousuarioid = tipoUsuarioId
            )
            firestore.collection(USERS_COLLECTION).document(firebaseUser.uid).set(usuarioData).await()
            Log.i(TAG, "Datos guardados en $USERS_COLLECTION.")

            return RegisterResult.Success

        } catch (e: Exception) {
            Log.e(TAG, "Error durante el registro completo", e)
            auth.currentUser?.delete()?.await() // Intenta revertir la creación en Auth si Firestore falla
            return when (e) {
                is FirebaseAuthWeakPasswordException -> RegisterResult.Failure(RegisterError.WEAK_PASSWORD, "Contraseña débil.")
                is FirebaseAuthUserCollisionException -> RegisterResult.Failure(RegisterError.EMAIL_ALREADY_EXISTS, "Correo ya existe.")
                is com.google.firebase.firestore.FirebaseFirestoreException -> RegisterResult.Failure(RegisterError.FIRESTORE_ERROR, "Error DB: ${e.message}")
                else -> RegisterResult.Failure(RegisterError.UNKNOWN, e.message ?: "Error desconocido.")
            }
        }
    }

    private fun parseDateToTimestamp(dateString: String): Timestamp? {
        return try {
            if (dateString.matches(Regex("^\\d{4}-\\d{2}-\\d{2}$"))) {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).apply { isLenient = false }
                sdf.parse(dateString)?.let { Timestamp(it) }
            } else {
                Log.w(TAG, "Formato de fecha inválido para Timestamp: $dateString")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al parsear fecha '$dateString' a Timestamp", e)
            null
        }
    }

    suspend fun getDocumentTypes(): List<String> {
        return try {
            val snapshot = firestore.collection(TIPOS_DOCUMENTO_FIRESTORE_COLLECTION).orderBy("descripcion").get().await()
            val types = snapshot.documents.mapNotNull { it.getString("descripcion") }
            if (types.isEmpty()) Log.w(TAG, "getDocumentTypes: Colección '${TIPOS_DOCUMENTO_FIRESTORE_COLLECTION}' vacía o sin campo 'descripcion'.")
            types
        } catch (e: Exception) {
            Log.e(TAG, "Error obteniendo tipos de documento de Firestore.", e)
            emptyList() // Devolver lista vacía en caso de error, el ViewModel/UI debe manejarlo
        }
    }

    suspend fun getGenders(): List<String> {
        return try {
            val snapshot = firestore.collection(GENEROS_FIRESTORE_COLLECTION).orderBy("descripcion").get().await()
            val gendersList = snapshot.documents.mapNotNull { it.getString("descripcion") }
            if (gendersList.isEmpty()) Log.w(TAG, "getGenders: Colección '${GENEROS_FIRESTORE_COLLECTION}' vacía o sin campo 'descripcion'.")
            gendersList
        } catch (e: Exception) {
            Log.e(TAG, "Error obteniendo géneros de Firestore.", e)
            emptyList()
        }
    }

    suspend fun getDepartments(): List<String> {
        return try {
            val snapshot = firestore.collection(UBIGEOS_COLLECTION).get().await()
            snapshot.documents.mapNotNull { it.getString("departamento") }.distinct().sorted()
        } catch (e: Exception) {
            Log.e(TAG, "Error obteniendo departamentos", e); emptyList()
        }
    }

    suspend fun getProvinces(department: String): List<String> {
        if (department.isBlank()) return emptyList()
        return try {
            val snapshot = firestore.collection(UBIGEOS_COLLECTION)
                .whereEqualTo("departamento", department)
                .get().await()
            snapshot.documents.mapNotNull { it.getString("provincia") }.distinct().sorted()
        } catch (e: Exception) {
            Log.e(TAG, "Error obteniendo provincias para $department", e); emptyList()
        }
    }

    suspend fun getDistricts(department: String, province: String): List<String> {
        if (department.isBlank() || province.isBlank()) return emptyList()
        return try {
            val snapshot = firestore.collection(UBIGEOS_COLLECTION)
                .whereEqualTo("departamento", department)
                .whereEqualTo("provincia", province)
                .get().await()
            snapshot.documents.mapNotNull { it.getString("distrito") }.distinct().sorted()
        } catch (e: Exception) {
            Log.e(TAG, "Error obteniendo distritos para $department/$province", e); emptyList()
        }
    }

    private suspend fun findUbigeoId(department: String, province: String, district: String): String? {
        if (department.isBlank() || province.isBlank() || district.isBlank()) return null
        return try {
            val snapshot = firestore.collection(UBIGEOS_COLLECTION)
                .whereEqualTo("departamento", department)
                .whereEqualTo("provincia", province)
                .whereEqualTo("distrito", district)
                .limit(1)
                .get().await()
            if (!snapshot.isEmpty) snapshot.documents[0].id else {
                Log.w(TAG, "No se encontró ubigeoid para $department/$province/$district"); null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error buscando ubigeoid", e); null
        }
    }

    private suspend fun findTipoUsuarioId(descripcion: String): String? {
        if (descripcion.isBlank()) return null
        return try {
            val snapshot = firestore.collection(TIPOS_USUARIO_COLLECTION)
                .whereEqualTo("descripcion", descripcion)
                .limit(1)
                .get().await()
            if (!snapshot.isEmpty) snapshot.documents[0].id else {
                Log.w(TAG, "No se encontró tipousuarioid para descripción: $descripcion"); null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error buscando tipousuarioid", e); null
        }
    }
}

// RegisterResult y RegisterError (sin cambios, como los tenías)
sealed class RegisterResult {
    object Success : RegisterResult()
    data class Failure(val errorType: RegisterError, val message: String? = null) : RegisterResult()
}

enum class RegisterError {
    WEAK_PASSWORD,
    EMAIL_ALREADY_EXISTS,
    EMPTY_CREDENTIALS,
    INVALID_DATA, // Para datos mal formateados, como fecha
    FIRESTORE_ERROR,
    NETWORK_ERROR, // Podrías añadir si detectas problemas de red específicos
    UNKNOWN
}