package com.example.sisvitag2.data.repository

import android.net.Uri
import android.util.Log
import com.example.sisvitag2.data.model.UserProfileData
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale

class AccountRepository(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage // Para la foto de perfil
) {
    companion object {
        private const val TAG = "AccountRepository"
        private const val USERS_COLLECTION = "usuarios" // Colección única para datos de usuario
        private const val UBIGEOS_COLLECTION = "ubigeos"
        private const val PROFILE_IMAGES_FOLDER = "profile_images"
    }

    suspend fun getUserProfile(): Result<UserProfileData> {
        val firebaseUser = auth.currentUser
        if (firebaseUser == null) {
            Log.w(TAG, "getUserProfile: Usuario no autenticado.")
            return Result.failure(Exception("Usuario no autenticado."))
        }

        return try {
            Log.d(TAG, "Obteniendo perfil de ${USERS_COLLECTION} para UID: ${firebaseUser.uid}")
            val userDocSnapshot = firestore.collection(USERS_COLLECTION)
                .document(firebaseUser.uid)
                .get()
                .await()

            if (userDocSnapshot.exists()) {
                val nombre = userDocSnapshot.getString("nombre") ?: ""
                val apellidoPaterno = userDocSnapshot.getString("apellidopaterno") ?: ""
                val apellidoMaterno = userDocSnapshot.getString("apellidomaterno")
                val fechaNacimientoTimestamp = userDocSnapshot.getTimestamp("fechanacimiento")
                val ubigeoId = userDocSnapshot.getString("ubigeoid")
                val tipoDocumento = userDocSnapshot.getString("tipo_documento")
                val numeroDocumento = userDocSnapshot.getString("numero_documento")
                val genero = userDocSnapshot.getString("genero")
                val telefono = userDocSnapshot.getString("telefono")
                val photoUrl = userDocSnapshot.getString("photoUrl") // Leer URL de foto desde Firestore
                // val correo = userDocSnapshot.getString("correo") // Ya lo tenemos de firebaseUser.email
                // val tipousuarioid = userDocSnapshot.getString("tipousuarioid") // Podrías cargarlo si lo necesitas en UserProfileData

                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val fechaNacimientoStr = fechaNacimientoTimestamp?.toDate()?.let { sdf.format(it) }

                var departamentoStr: String? = null
                var provinciaStr: String? = null
                var distritoStr: String? = null

                if (!ubigeoId.isNullOrBlank()) {
                    try {
                        val ubigeoDoc = firestore.collection(UBIGEOS_COLLECTION).document(ubigeoId).get().await()
                        if (ubigeoDoc.exists()) {
                            departamentoStr = ubigeoDoc.getString("departamento")
                            provinciaStr = ubigeoDoc.getString("provincia")
                            distritoStr = ubigeoDoc.getString("distrito")
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "Error al obtener detalles del ubigeo $ubigeoId", e)
                    }
                }

                val userProfile = UserProfileData(
                    uid = firebaseUser.uid,
                    email = firebaseUser.email,
                    displayName = firebaseUser.displayName.takeIf { !it.isNullOrBlank() } ?: "$nombre $apellidoPaterno".trim(),
                    photoUrl = photoUrl ?: firebaseUser.photoUrl?.toString(), // Priorizar URL de Firestore
                    nombre = nombre,
                    apellidoPaterno = apellidoPaterno,
                    apellidoMaterno = apellidoMaterno,
                    fechaNacimiento = fechaNacimientoTimestamp,
                    tipoDocumento = tipoDocumento,
                    numeroDocumento = numeroDocumento,
                    genero = genero,
                    telefono = telefono,
                    departamento = departamentoStr,
                    provincia = provinciaStr,
                    distrito = distritoStr,
                    ubigeoid = ubigeoId // <-- Asignar el ubigeoid real
                )
                Log.i(TAG, "Perfil construido exitosamente desde ${USERS_COLLECTION} para ${firebaseUser.uid}")
                Result.success(userProfile)
            } else {
                Log.w(TAG, "No se encontró el documento '${USERS_COLLECTION}' para el usuario: ${firebaseUser.uid}")
                Result.failure(Exception("Datos de perfil no encontrados en la base de datos."))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Excepción al obtener el perfil del usuario ${firebaseUser.uid}", e)
            Result.failure(e)
        }
    }

    suspend fun updateUserPersonalData(userId: String, data: Map<String, Any?>): Boolean {
        return try {
            val dataToUpdate = data.toMutableMap()
            // Asegurarse de que fechanacimiento sea Timestamp
            val fechaNacimiento = dataToUpdate["fechanacimiento"]
            if (fechaNacimiento is String) {
                // Intentar parsear la fecha a Timestamp (asumiendo formato yyyy-MM-dd)
                try {
                    val parts = fechaNacimiento.split("-")
                    if (parts.size == 3) {
                        val year = parts[0].toInt()
                        val month = parts[1].toInt() - 1 // Mes base 0
                        val day = parts[2].toInt()
                        val cal = java.util.Calendar.getInstance()
                        cal.set(year, month, day, 0, 0, 0)
                        dataToUpdate["fechanacimiento"] = Timestamp(cal.time)
                    }
                } catch (e: Exception) {
                    // Si falla, eliminar el campo para evitar error de tipo
                    dataToUpdate.remove("fechanacimiento")
                }
            }
            // Eliminar campos nulos para evitar errores
            dataToUpdate.entries.removeIf { it.value == null }
            firestore.collection("usuarios").document(userId).update(dataToUpdate).await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error al actualizar datos personales", e)
            false
        }
    }

    suspend fun updateUserProfilePicture(userId: String, imageUri: Uri): Result<String> {
        if (userId.isBlank()) return Result.failure(IllegalArgumentException("User ID no puede estar vacío"))
        return try {
            Log.d(TAG, "Iniciando subida de foto de perfil para usuario: $userId")
            Log.d(TAG, "URI de imagen: $imageUri")
            val storageRef = storage.reference.child("$PROFILE_IMAGES_FOLDER/$userId.jpg")
            storageRef.putFile(imageUri).await()
            val downloadUrl = storageRef.downloadUrl.await().toString()

            auth.currentUser?.takeIf { it.uid == userId }?.let { user ->
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setPhotoUri(Uri.parse(downloadUrl))
                    .build()
                user.updateProfile(profileUpdates).await()
                Log.i(TAG, "Firebase Auth photoURL actualizado.")
                
                // Actualizar en Firestore también para persistencia
                firestore.collection(USERS_COLLECTION).document(userId).update("photoUrl", downloadUrl).await()
                Log.i(TAG, "URL de foto actualizada en Firestore.")
            }
            Result.success(downloadUrl)
        } catch (e: Exception) {
            Log.e(TAG, "Error actualizando foto de perfil para $userId", e)
            Result.failure(e)
        }
    }

    /**
     * Actualiza el rol y el estado de un usuario (solo para administrador)
     */
    suspend fun updateUserRoleAndStatus(userId: String, newRole: Int, newStatus: String): Result<Unit> {
        return try {
            val updates = mapOf(
                "legacyTipoUsuarioId" to newRole,
                "estado" to newStatus
            )
            firestore.collection(USERS_COLLECTION).document(userId).update(updates).await()
            Log.i(TAG, "Rol y estado actualizados para usuario $userId: rol=$newRole, estado=$newStatus")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error actualizando rol/estado para $userId", e)
            Result.failure(e)
        }
    }

    /**
     * Elimina un usuario de Firestore y de Firebase Auth (solo para administrador)
     * Nota: Para eliminar de Auth, se requiere autenticación como ese usuario o privilegios elevados (Cloud Functions recomendado para producción)
     */
    suspend fun deleteUserCompletely(userId: String): Result<Unit> {
        return try {
            // Eliminar de Firestore
            firestore.collection(USERS_COLLECTION).document(userId).delete().await()
            Log.i(TAG, "Usuario $userId eliminado de Firestore.")
            // Eliminar de Auth (solo si el usuario actual es el mismo o tienes privilegios)
            val userToDelete = auth.currentUser
            if (userToDelete != null && userToDelete.uid == userId) {
                userToDelete.delete().await()
                Log.i(TAG, "Usuario $userId eliminado de Firebase Auth.")
            } else {
                Log.w(TAG, "No se puede eliminar de Auth a menos que estés autenticado como ese usuario. Para producción, usar Cloud Functions.")
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error eliminando usuario $userId", e)
            Result.failure(e)
        }
    }

    /**
     * Obtiene la lista de todos los usuarios (solo para administrador)
     */
    suspend fun getAllUsers(): Result<List<UserProfileData>> {
        return try {
            val snapshot = firestore.collection(USERS_COLLECTION).get().await()
            val users = snapshot.documents.mapNotNull { doc ->
                val uid = doc.getString("uid") ?: doc.id
                val email = doc.getString("correo")
                val nombre = doc.getString("nombre") ?: ""
                val apellidoPaterno = doc.getString("apellidopaterno") ?: ""
                val apellidoMaterno = doc.getString("apellidomaterno")
                val displayName = "$nombre $apellidoPaterno".trim()
                val photoUrl = doc.getString("photoUrl")
                val fechaNacimiento = doc.getTimestamp("fechanacimiento")
                val tipoDocumento = doc.getString("tipo_documento")
                val numeroDocumento = doc.getString("numero_documento")
                val genero = doc.getString("genero")
                val telefono = doc.getString("telefono")
                val departamento = doc.getString("departamento")
                val provincia = doc.getString("provincia")
                val distrito = doc.getString("distrito")
                val estado = doc.getString("estado")
                val legacyTipoUsuarioId = doc.getLong("legacyTipoUsuarioId")?.toInt()
                UserProfileData(
                    uid = uid,
                    email = email,
                    displayName = displayName,
                    photoUrl = photoUrl,
                    nombre = nombre,
                    apellidoPaterno = apellidoPaterno,
                    apellidoMaterno = apellidoMaterno,
                    fechaNacimiento = fechaNacimiento,
                    tipoDocumento = tipoDocumento,
                    numeroDocumento = numeroDocumento,
                    genero = genero,
                    telefono = telefono,
                    departamento = departamento,
                    provincia = provincia,
                    distrito = distrito,
                    estado = estado,
                    legacyTipoUsuarioId = legacyTipoUsuarioId
                )
            }
            Result.success(users)
        } catch (e: Exception) {
            Log.e(TAG, "Error obteniendo lista de usuarios", e)
            Result.failure(e)
        }
    }
}