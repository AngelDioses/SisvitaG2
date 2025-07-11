package com.example.sisvitag2.data.repository.emotionalAnalysis

import android.net.Uri
import android.util.Log
import com.example.sisvitag2.data.repository.emotionalAnalysis.EmotionalAnalysisResponse // Ajusta import si es necesario
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.toObject // Importación necesaria
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf // Importar flowOf
import kotlinx.coroutines.tasks.await
import java.util.UUID

/**
 * Data class auxiliar para representar la estructura del documento
 * en la colección 'analisisResultados' de Firestore.
 */
data class FirestoreAnalysisResult(
    var userId: String = "",
    var storagePath: String = "",
    var status: String = "pendiente", // Estados: pendiente, subido, procesando, completado, error
    var resultados: EmotionalAnalysisResponse? = null, // Objeto embebido con las emociones
    var error: String? = null, // Campo para mensajes de error del procesamiento
    @com.google.firebase.firestore.ServerTimestamp // Marca para que Firestore ponga el timestamp
    var timestamp: com.google.firebase.Timestamp? = null // Timestamp del servidor
) {
    // Constructor sin argumentos requerido por Firestore para deserialización
    constructor() : this("", "", "pendiente", null, null, null)
}

/**
 * Repositorio para manejar las operaciones relacionadas con el análisis de emociones,
 * interactuando con Firebase Storage y Firestore, y ahora también con la nueva API externa.
 */
class EmotionalAnalysisRepository(
    // Inyecta las dependencias de Firebase (vía Koin)
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val apiService: EmotionalAnalysisApiService // Nueva dependencia
) {

    companion object {
        private const val TAG = "EmotionalAnalysisRepo"
        // Nombre de la colección en Firestore donde se guardan los resultados/estados
        private const val RESULTS_COLLECTION = "analisisResultados"
        // Carpeta raíz en Firebase Storage donde se suben los videos
        private const val VIDEOS_STORAGE_PATH = "videos"
    }

    /**
     * NUEVO MÉTODO: Analiza una imagen directamente usando la API externa
     * sin subir a Firebase Storage
     */
    suspend fun analyzeImageDirectly(imageUri: Uri): EmotionalAnalysisResponse {
        Log.d(TAG, "Iniciando análisis directo de imagen: $imageUri")
        
        return try {
            // Llamar directamente a la API externa
            val result = apiService.analyzeImage(imageUri)
            Log.i(TAG, "Análisis directo de imagen completado exitosamente")
            result
        } catch (e: Exception) {
            Log.e(TAG, "Error en análisis directo de imagen", e)
            throw e
        }
    }

    /**
     * NUEVO MÉTODO: Analiza un video directamente usando la API externa
     * sin subir a Firebase Storage
     */
    suspend fun analyzeVideoDirectly(videoUri: Uri): EmotionalAnalysisResponse {
        Log.d(TAG, "Iniciando análisis directo de video: $videoUri")
        
        return try {
            // Llamar directamente a la API externa
            val result = apiService.analyzeVideo(videoUri)
            Log.i(TAG, "Análisis directo completado exitosamente")
            result
        } catch (e: Exception) {
            Log.e(TAG, "Error en análisis directo", e)
            throw e
        }
    }

    /**
     * Sube un video a Firebase Storage e inicializa un documento en Firestore
     * para rastrear el estado del análisis. Esta función es llamada típicamente
     * después de que el usuario graba un video.
     *
     * @param videoUri La Uri local del archivo de video a subir.
     * @return El ID (String) del documento creado en Firestore para este análisis,
     *         o null si el usuario no está autenticado o si ocurre un error durante
     *         la subida o la creación inicial del documento.
     */
    suspend fun uploadVideoForAnalysis(videoUri: Uri): String? {
        val userId = firebaseAuth.currentUser?.uid
        // Verifica que el usuario esté autenticado
        if (userId == null) {
            Log.w(TAG, "Usuario no autenticado. No se puede subir el video.")
            return null
        }

        // Genera un ID único para este análisis específico y para el documento Firestore
        val videoId = UUID.randomUUID().toString()
        // Crea una ruta única en Storage para evitar colisiones
        val storagePath = "$VIDEOS_STORAGE_PATH/$userId/$videoId.mp4"

        Log.d(TAG, "Iniciando subida a Storage: $storagePath para videoId: $videoId")

        return try {
            // 1. Obtiene la referencia en Storage y sube el archivo
            val storageRef = storage.reference.child(storagePath)
            storageRef.putFile(videoUri).await() // Espera a que la subida se complete
            Log.i(TAG, "Video subido exitosamente a: $storagePath")

            // 2. Prepara los datos iniciales para el documento en Firestore
            val initialData = FirestoreAnalysisResult(
                userId = userId,
                storagePath = storagePath,
                status = "subido" // Estado inicial después de subir, antes del procesamiento por Cloud Function
                // timestamp se establecerá automáticamente por @ServerTimestamp
            )
            // 3. Crea el documento en Firestore usando el videoId generado
            val docRef = firestore.collection(RESULTS_COLLECTION).document(videoId)
            docRef.set(initialData).await() // Espera a que se cree el documento
            Log.i(TAG, "Documento inicial creado en Firestore con ID: $videoId")

            // 4. Devuelve el ID del documento para seguimiento
            videoId

        } catch (e: Exception) {
            // Captura cualquier error durante la subida a Storage o escritura a Firestore
            Log.e(TAG, "Error durante la subida ($storagePath) o creación del documento inicial ($videoId)", e)
            // Consideración: Si la subida a Storage tuvo éxito pero la escritura a Firestore falló,
            // podrías tener un video "huérfano" en Storage. Podrías añadir lógica para intentar borrarlo.
            // storage.reference.child(storagePath).delete().await() // Ejemplo (con manejo de errores adicional)
            null // Indica que ocurrió un error
        }
    }

    /**
     * Obtiene un Flow de Kotlin que emite actualizaciones en tiempo real del estado
     * y resultado del análisis para un videoId específico. Escucha cambios en el
     * documento correspondiente en Firestore. Usado por LoadingViewModel.
     *
     * @param videoId El ID (String) del documento a observar en la colección 'analisisResultados'.
     * @return Un Flow que emite objetos `FirestoreAnalysisResult?`. Emitirá `null` si el
     *         documento no existe o es eliminado. El Flow se cierra con error si ocurre
     *         un problema en el listener de Firestore.
     */
    fun getAnalysisResultsFlow(videoId: String?): Flow<FirestoreAnalysisResult?> {
        // Si el videoId es nulo o vacío, devuelve un Flow que emite null inmediatamente
        if (videoId.isNullOrBlank()) {
            Log.w(TAG, "getAnalysisResultsFlow llamado con videoId nulo o vacío.")
            return flowOf(null)
        }

        Log.d(TAG, "Creando Flow para escuchar documento: $videoId")
        val docRef = firestore.collection(RESULTS_COLLECTION).document(videoId)

        // callbackFlow convierte el listener asíncrono de Firestore en un Flow frío
        return callbackFlow {
            // Registra el listener que se activa cada vez que el documento cambia
            val listenerRegistration: ListenerRegistration = docRef.addSnapshotListener { snapshot, error ->
                // Manejo de errores del listener
                if (error != null) {
                    Log.e(TAG, "Error en listener para documento $videoId", error)
                    close(error) // Cierra el Flow con la excepción
                    return@addSnapshotListener
                }

                // Manejo del snapshot recibido
                if (snapshot != null && snapshot.exists()) {
                    // Intenta convertir el snapshot al objeto FirestoreAnalysisResult
                    val result = snapshot.toObject<FirestoreAnalysisResult>()
                    Log.d(TAG, "Snapshot recibido para $videoId. Status=${result?.status}")
                    // Emite el resultado (o null si la conversión falla) al Flow
                    trySend(result).isSuccess // Usa trySend para manejo seguro en coroutines
                } else {
                    // El documento no existe o fue borrado
                    Log.w(TAG, "Documento $videoId no encontrado o eliminado.")
                    // Emite null para indicar que no hay datos
                    trySend(null).isSuccess
                }
            }

            // Este bloque se ejecuta cuando el Flow es cancelado (ya no hay colectores)
            awaitClose {
                Log.d(TAG, "Cancelando listener de Firestore para $videoId")
                // Es CRUCIAL remover el listener para evitar fugas de memoria y lecturas innecesarias
                listenerRegistration.remove()
            }
        }
    }

    /**
     * Obtiene un resultado de análisis específico por su ID de documento desde Firestore.
     * Realiza una lectura única, no escucha cambios. Usado por ResultsViewModel.
     *
     * @param videoId El ID (String) del documento en la colección 'analisisResultados'.
     * @return El objeto `FirestoreAnalysisResult` si se encuentra y la conversión es exitosa,
     *         o `null` si el documento no existe o si ocurre algún error durante la lectura.
     */
    suspend fun getAnalysisResultById(videoId: String): FirestoreAnalysisResult? {
        // Verifica que el ID no sea inválido
        if (videoId.isBlank()) {
            Log.w(TAG, "getAnalysisResultById llamado con videoId vacío.")
            return null
        }
        Log.d(TAG, "Obteniendo datos (lectura única) para análisis ID: $videoId")
        return try {
            // Obtiene la referencia al documento
            val docRef = firestore.collection(RESULTS_COLLECTION).document(videoId)
            // Realiza la lectura del documento
            val snapshot = docRef.get().await() // Espera el resultado

            // Verifica si el documento existe
            if (snapshot.exists()) {
                // Intenta convertir el snapshot al objeto FirestoreAnalysisResult
                val result = snapshot.toObject<FirestoreAnalysisResult>()
                Log.i(TAG, "Documento encontrado para ID: $videoId. Status: ${result?.status}")
                result // Devuelve el objeto deserializado
            } else {
                Log.w(TAG, "Documento no encontrado para ID: $videoId (en getAnalysisResultById)")
                null // Devuelve null si el documento no existe
            }
        } catch (e: Exception) {
            // Captura cualquier error durante la operación de Firestore
            Log.e(TAG, "Error obteniendo análisis por ID: $videoId", e)
            null // Devuelve null en caso de error
        }
    }
}