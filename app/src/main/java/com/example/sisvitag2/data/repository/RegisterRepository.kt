package com.example.sisvitag2.data.repository

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale

// Definición de los resultados y errores del registro
sealed class RegisterResult {
    object Success : RegisterResult()
    data class Failure(val errorType: RegisterError, val message: String? = null) : RegisterResult()
}

enum class RegisterError {
    WEAK_PASSWORD,
    EMAIL_ALREADY_EXISTS,
    EMPTY_CREDENTIALS,
    INVALID_DATA,
    FIRESTORE_ERROR,
    NETWORK_ERROR,
    UNKNOWN,
    ALL_QUESTIONS_NOT_ANSWERED // Aunque es más para TestRepository, no hace daño tenerlo
}

class RegisterRepository(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {

    companion object {
        private const val TAG = "RegisterRepository"
        private const val USERS_COLLECTION = "usuarios"
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
        Log.d(TAG, "----------------------------------------------------")
        Log.d(TAG, "INICIO PROCESO DE REGISTRO para Email: $email")
        Log.d(TAG, "Detalles recibidos: $profileDetails")
        Log.d(TAG, "----------------------------------------------------")

        if (email.isBlank() || password.isBlank()) {
            Log.w(TAG, "VALIDACIÓN INICIAL: Email o contraseña vacíos.")
            return RegisterResult.Failure(RegisterError.EMPTY_CREDENTIALS, "Email o contraseña vacíos.")
        }

        var tempFirebaseUser: com.google.firebase.auth.FirebaseUser? = null

        try {
            Log.d(TAG, "PASO 1: Intentando crear usuario en Firebase Auth...")
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            tempFirebaseUser = authResult.user
            if (tempFirebaseUser == null) {
                Log.e(TAG, "PASO 1: Falló la creación en Auth, firebaseUser es null.")
                return RegisterResult.Failure(RegisterError.UNKNOWN, "Error inesperado al crear usuario en Auth.")
            }
            Log.i(TAG, "PASO 1: Usuario CREADO en Auth con UID: ${tempFirebaseUser.uid}")

            val nombreAuth = profileDetails["nombre"] as? String ?: ""
            val apellidoPaternoAuth = profileDetails["apellidopaterno"] as? String ?: ""
            val nombreCompletoAuth = "$nombreAuth $apellidoPaternoAuth".trim()

            if (nombreCompletoAuth.isNotBlank()) {
                Log.d(TAG, "PASO 2: Intentando actualizar displayName en Auth a: '$nombreCompletoAuth'")
                val profileUpdates = UserProfileChangeRequest.Builder().setDisplayName(nombreCompletoAuth).build()
                try {
                    tempFirebaseUser.updateProfile(profileUpdates).await() // Esperar a que se complete
                    Log.i(TAG, "PASO 2: Firebase Auth displayName ACTUALIZADO.")
                } catch (e: Exception) {
                    Log.w(TAG, "PASO 2: EXCEPCIÓN al actualizar displayName en Auth (no fatal para el registro, pero se loguea): ${e.message}", e)
                }
            } else {
                Log.w(TAG, "PASO 2: Nombre completo para displayName vacío, no se actualizó en Auth.")
            }

            Log.d(TAG, "PASO 3: Obteniendo IDs referenciales (Ubigeo, TipoUsuario)...")
            val departamento = profileDetails["departamento"] as? String ?: ""
            val provincia = profileDetails["provincia"] as? String ?: ""
            val distrito = profileDetails["distrito"] as? String ?: ""
            Log.d(TAG, "PASO 3: Buscando ubigeoId para: Dpto='$departamento', Prov='$provincia', Dist='$distrito'")
            val ubigeoId = findUbigeoId(departamento, provincia, distrito)
            if (ubigeoId == null) {
                Log.e(TAG, "PASO 3: FALLÓ obtención de ubigeoId. Datos: D='$departamento', P='$provincia', Dt='$distrito'")
                throw Exception("Ubicación no válida. Revisa departamento, provincia y distrito.")
            }
            Log.i(TAG, "PASO 3: ubigeoId ENCONTRADO: $ubigeoId")

            val tipoUsuarioDesc = profileDetails["role_description"] as? String ?: "Paciente"
            Log.d(TAG, "PASO 3: Buscando tipoUsuarioId para descripción: '$tipoUsuarioDesc'")
            val tipoUsuarioId = findTipoUsuarioId(tipoUsuarioDesc)
            if (tipoUsuarioId == null) {
                Log.e(TAG, "PASO 3: FALLÓ obtención de tipoUsuarioId para '$tipoUsuarioDesc'.")
                throw Exception("Tipo de usuario ('$tipoUsuarioDesc') no es válido.")
            }
            Log.i(TAG, "PASO 3: tipoUsuarioId ENCONTRADO: $tipoUsuarioId")

            Log.d(TAG, "PASO 4: Procesando fecha de nacimiento...")
            val birthDateString = profileDetails["fechanacimiento_str"] as? String
            if (birthDateString.isNullOrBlank()) {
                Log.e(TAG, "PASO 4: 'fechanacimiento_str' está vacía o nula.")
                throw Exception("La fecha de nacimiento es obligatoria.")
            }
            val birthDateTimestamp = parseDateToTimestamp(birthDateString)
            if (birthDateTimestamp == null) {
                Log.e(TAG, "PASO 4: FALLÓ la conversión de '$birthDateString' a Timestamp.")
                throw Exception("Formato de fecha de nacimiento inválido (esperado YYYY-MM-DD).")
            }
            Log.i(TAG, "PASO 4: birthDateTimestamp CREADO: $birthDateTimestamp")

            Log.d(TAG, "PASO 5: Construyendo userDocumentData...")
            val userDocumentData = mutableMapOf<String, Any>(
                "uid" to tempFirebaseUser.uid,
                "correo" to tempFirebaseUser.email!!,
                "tipousuarioid" to tipoUsuarioId,
                "nombre" to (profileDetails["nombre"] as? String ?: ""),
                "apellidopaterno" to (profileDetails["apellidopaterno"] as? String ?: ""),
                "fechanacimiento" to birthDateTimestamp,
                "ubigeoid" to ubigeoId,
                "tipo_documento" to (profileDetails["tipo_documento"] as? String ?: ""),
                "numero_documento" to (profileDetails["numero_documento"] as? String ?: ""),
                "genero" to (profileDetails["genero"] as? String ?: "")
            )
            (profileDetails["apellidomaterno"] as? String)?.takeIf { it.isNotBlank() }?.let {
                userDocumentData["apellidomaterno"] = it
            }
            (profileDetails["telefono"] as? String)?.takeIf { it.isNotBlank() }?.let {
                userDocumentData["telefono"] = it
            }
            Log.d(TAG, "PASO 5: userDocumentData CONSTRUIDO: $userDocumentData")

            Log.i(TAG, "PASO 6: INTENTANDO GUARDAR en Firestore (${USERS_COLLECTION}/${tempFirebaseUser.uid}).")
            firestore.collection(USERS_COLLECTION).document(tempFirebaseUser.uid).set(userDocumentData).await()
            Log.i(TAG, "PASO 6: ÉXITO al guardar datos en ${USERS_COLLECTION}/${tempFirebaseUser.uid}.")
            Log.d(TAG, "----------------------------------------------------")
            Log.d(TAG, "FIN PROCESO DE REGISTRO EXITOSO para Email: $email")
            Log.d(TAG, "----------------------------------------------------")
            return RegisterResult.Success

        } catch (e: Exception) {
            Log.e(TAG, "EXCEPCIÓN GENERAL durante el registro completo: ${e.javaClass.simpleName} - ${e.message}", e)
            tempFirebaseUser?.delete()?.addOnCompleteListener { deleteTask ->
                if (deleteTask.isSuccessful) Log.i(TAG, "Usuario de Auth (si existía) ${tempFirebaseUser?.uid} eliminado después de fallo.")
                else Log.w(TAG, "No se pudo eliminar usuario de Auth ${tempFirebaseUser?.uid} (si existía) después de fallo.", deleteTask.exception)
            }
            val errorType = when (e) {
                is FirebaseAuthWeakPasswordException -> RegisterError.WEAK_PASSWORD
                is FirebaseAuthUserCollisionException -> RegisterError.EMAIL_ALREADY_EXISTS
                is com.google.firebase.firestore.FirebaseFirestoreException -> {
                    Log.e(TAG, "FirebaseFirestoreException code: ${e.code}, message: ${e.message}")
                    RegisterError.FIRESTORE_ERROR
                }
                else -> if (e.message?.contains("Ubicación no válida") == true ||
                    e.message?.contains("Tipo de usuario no válido") == true ||
                    e.message?.contains("fecha de nacimiento es obligatoria") == true ||
                    e.message?.contains("Formato de fecha de nacimiento inválido") == true) {
                    RegisterError.INVALID_DATA
                } else {
                    RegisterError.UNKNOWN
                }
            }
            val customMessage = when (errorType) {
                RegisterError.WEAK_PASSWORD -> "La contraseña es muy débil (mínimo 6 caracteres)."
                RegisterError.EMAIL_ALREADY_EXISTS -> "El correo electrónico ya está en uso."
                RegisterError.FIRESTORE_ERROR -> "Error al guardar sus datos en el servidor. Por favor, inténtelo de nuevo."
                RegisterError.INVALID_DATA -> e.message // Usar el mensaje de la excepción que lanzamos
                else -> e.message ?: "Ocurrió un error inesperado durante el registro."
            }
            return RegisterResult.Failure(errorType, customMessage)
        }
    }

    private fun parseDateToTimestamp(dateString: String): Timestamp? {
        Log.d(TAG, "parseDateToTimestamp: Intentando parsear '$dateString'")
        return try {
            if (dateString.matches(Regex("^\\d{4}-\\d{2}-\\d{2}$"))) {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).apply { isLenient = false }
                val parsedDate = sdf.parse(dateString)
                if (parsedDate != null) {
                    Log.d(TAG, "parseDateToTimestamp: Parseo exitoso a $parsedDate")
                    Timestamp(parsedDate)
                } else {
                    Log.w(TAG, "parseDateToTimestamp: sdf.parse devolvió null para '$dateString'")
                    null
                }
            } else {
                Log.w(TAG, "parseDateToTimestamp: Formato de fecha inválido: '$dateString', no coincide con YYYY-MM-DD")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "parseDateToTimestamp: Excepción parseando '$dateString'", e)
            null
        }
    }

    suspend fun getDocumentTypes(): List<String> {
        Log.d(TAG, "getDocumentTypes: Iniciando carga...")
        return try {
            val snapshot = firestore.collection(TIPOS_DOCUMENTO_FIRESTORE_COLLECTION).orderBy("descripcion").get().await()
            val types = snapshot.documents.mapNotNull { it.getString("descripcion") }
            if (types.isEmpty()) Log.w(TAG, "getDocumentTypes: Colección '${TIPOS_DOCUMENTO_FIRESTORE_COLLECTION}' vacía o sin campo 'descripcion'.")
            else Log.i(TAG, "getDocumentTypes: Obtenidos ${types.size} tipos: $types")
            types
        } catch (e: Exception) {
            Log.e(TAG, "getDocumentTypes: Error obteniendo de Firestore.", e); emptyList()
        }
    }

    suspend fun getGenders(): List<String> {
        Log.d(TAG, "getGenders: Iniciando carga...")
        return try {
            val snapshot = firestore.collection(GENEROS_FIRESTORE_COLLECTION).orderBy("descripcion").get().await()
            val gendersList = snapshot.documents.mapNotNull { it.getString("descripcion") }
            if (gendersList.isEmpty()) Log.w(TAG, "getGenders: Colección '${GENEROS_FIRESTORE_COLLECTION}' vacía o sin campo 'descripcion'.")
            else Log.i(TAG, "getGenders: Obtenidos ${gendersList.size} géneros: $gendersList")
            gendersList
        } catch (e: Exception) {
            Log.e(TAG, "getGenders: Error obteniendo de Firestore.", e); emptyList()
        }
    }

    suspend fun getDepartments(): List<String> {
        Log.d(TAG, "getDepartments: Iniciando carga...")
        return try {
            val snapshot = firestore.collection(UBIGEOS_COLLECTION).get().await()
            val departments = snapshot.documents.mapNotNull { it.getString("departamento") }.distinct().sorted()
            if (departments.isEmpty()) Log.w(TAG, "getDepartments: Colección '${UBIGEOS_COLLECTION}' vacía o sin campo 'departamento'.")
            else Log.i(TAG, "getDepartments: Obtenidos ${departments.size} departamentos.")
            departments
        } catch (e: Exception) {
            Log.e(TAG, "getDepartments: Error obteniendo de Firestore.", e); emptyList()
        }
    }

    suspend fun getProvinces(department: String): List<String> {
        if (department.isBlank()) return emptyList()
        Log.d(TAG, "getProvinces: Iniciando carga para departamento '$department'")
        return try {
            val snapshot = firestore.collection(UBIGEOS_COLLECTION)
                .whereEqualTo("departamento", department)
                .orderBy("provincia", Query.Direction.ASCENDING)
                .get().await()
            val provinces = snapshot.documents.mapNotNull { it.getString("provincia") }.distinct()
            Log.i(TAG, "getProvinces: Obtenidas ${provinces.size} provincias para '$department'.")
            provinces
        } catch (e: Exception) {
            Log.e(TAG, "getProvinces: Error obteniendo para '$department'.", e); emptyList()
        }
    }

    suspend fun getDistricts(department: String, province: String): List<String> {
        if (department.isBlank() || province.isBlank()) return emptyList()
        Log.d(TAG, "getDistricts: Iniciando carga para Dpto='$department', Prov='$province'")
        return try {
            val snapshot = firestore.collection(UBIGEOS_COLLECTION)
                .whereEqualTo("departamento", department)
                .whereEqualTo("provincia", province)
                .orderBy("distrito", Query.Direction.ASCENDING)
                .get().await()
            val districts = snapshot.documents.mapNotNull { it.getString("distrito") }.distinct()
            Log.i(TAG, "getDistricts: Obtenidos ${districts.size} distritos para '$department'/'$province'.")
            districts
        } catch (e: Exception) {
            Log.e(TAG, "getDistricts: Error obteniendo para '$department'/'$province'.", e); emptyList()
        }
    }

    private suspend fun findUbigeoId(department: String, province: String, district: String): String? {
        if (department.isBlank() || province.isBlank() || district.isBlank()) {
            Log.w(TAG, "findUbigeoId: Datos de ubicación incompletos. Dpto='$department', Prov='$province', Dist='$district'")
            return null
        }
        Log.d(TAG, "findUbigeoId: Buscando para D='$department', P='$province', Dt='$district'")
        return try {
            val snapshot = firestore.collection(UBIGEOS_COLLECTION)
                .whereEqualTo("departamento", department)
                .whereEqualTo("provincia", province)
                .whereEqualTo("distrito", district)
                .limit(1)
                .get().await()
            if (!snapshot.isEmpty) snapshot.documents[0].id.also { Log.i(TAG, "findUbigeoId: Encontrado ID '$it'") }
            else { Log.w(TAG, "findUbigeoId: No se encontró documento para D='$department', P='$province', Dt='$district'"); null }
        } catch (e: Exception) {
            Log.e(TAG, "findUbigeoId: Excepción buscando para D='$department', P='$province', Dt='$district'", e)
            throw e // Relanzar para que el catch principal lo maneje
        }
    }

    private suspend fun findTipoUsuarioId(descripcion: String): String? {
        if (descripcion.isBlank()) {
            Log.w(TAG, "findTipoUsuarioId: Descripción de tipo de usuario vacía.")
            return null
        }
        Log.d(TAG, "findTipoUsuarioId: Buscando para descripción '$descripcion'")
        return try {
            val snapshot = firestore.collection(TIPOS_USUARIO_COLLECTION)
                .whereEqualTo("descripcion", descripcion)
                .limit(1)
                .get().await()
            if (!snapshot.isEmpty) snapshot.documents[0].id.also { Log.i(TAG, "findTipoUsuarioId: Encontrado ID '$it' para '$descripcion'") }
            else { Log.w(TAG, "findTipoUsuarioId: No se encontró documento para descripción '$descripcion'"); null }
        } catch (e: Exception) {
            Log.e(TAG, "findTipoUsuarioId: Excepción buscando para '$descripcion'", e)
            throw e // Relanzar
        }
    }
}