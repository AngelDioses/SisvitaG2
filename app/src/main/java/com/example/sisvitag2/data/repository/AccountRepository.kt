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
                    photoUrl = firebaseUser.photoUrl?.toString(),
                    nombre = nombre,
                    apellidoPaterno = apellidoPaterno,
                    apellidoMaterno = apellidoMaterno,
                    fechaNacimiento = fechaNacimientoStr,
                    tipoDocumento = tipoDocumento,
                    numeroDocumento = numeroDocumento,
                    genero = genero,
                    telefono = telefono,
                    departamento = departamentoStr,
                    provincia = provinciaStr,
                    distrito = distritoStr
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

    suspend fun updateUserPersonalData(userId: String, dataToUpdate: Map<String, Any>): Result<Unit> {
        if (userId.isBlank()) return Result.failure(IllegalArgumentException("User ID no puede estar vacío"))
        if (dataToUpdate.isEmpty()) return Result.success(Unit)

        return try {
            Log.d(TAG, "Actualizando datos en ${USERS_COLLECTION} para $userId: $dataToUpdate")
            firestore.collection(USERS_COLLECTION).document(userId)
                .update(dataToUpdate)
                .await()

            val newNombre = dataToUpdate["nombre"] as? String
            val newApellidoPaterno = dataToUpdate["apellidopaterno"] as? String

            if (newNombre != null || newApellidoPaterno != null) {
                val firebaseUser = auth.currentUser
                if (firebaseUser != null && firebaseUser.uid == userId) {
                    val userDoc = firestore.collection(USERS_COLLECTION).document(userId).get().await()
                    val finalNombre = newNombre ?: userDoc.getString("nombre") ?: ""
                    val finalApellidoPaterno = newApellidoPaterno ?: userDoc.getString("apellidopaterno") ?: ""
                    val newDisplayName = "$finalNombre $finalApellidoPaterno".trim()

                    if (newDisplayName.isNotBlank() && newDisplayName != firebaseUser.displayName) {
                        val profileUpdates = UserProfileChangeRequest.Builder()
                            .setDisplayName(newDisplayName)
                            .build()
                        firebaseUser.updateProfile(profileUpdates).await()
                        Log.i(TAG, "Firebase Auth displayName actualizado a: $newDisplayName")
                    }
                }
            }
            Log.i(TAG, "Datos personales actualizados exitosamente en ${USERS_COLLECTION} para $userId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error actualizando datos personales para $userId", e)
            Result.failure(e)
        }
    }

    suspend fun updateUserProfilePicture(userId: String, imageUri: Uri): Result<String> {
        if (userId.isBlank()) return Result.failure(IllegalArgumentException("User ID no puede estar vacío"))
        return try {
            val storageRef = storage.reference.child("$PROFILE_IMAGES_FOLDER/$userId.jpg")
            storageRef.putFile(imageUri).await()
            val downloadUrl = storageRef.downloadUrl.await().toString()

            auth.currentUser?.takeIf { it.uid == userId }?.let { user ->
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setPhotoUri(Uri.parse(downloadUrl))
                    .build()
                user.updateProfile(profileUpdates).await()
                Log.i(TAG, "Firebase Auth photoURL actualizado.")
                // Opcional: Actualizar en Firestore también si guardas la URL allí
                // firestore.collection(USERS_COLLECTION).document(userId).update("photoUrl", downloadUrl).await()
            }
            Result.success(downloadUrl)
        } catch (e: Exception) {
            Log.e(TAG, "Error actualizando foto de perfil para $userId", e)
            Result.failure(e)
        }
    }
}