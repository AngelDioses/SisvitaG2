package com.example.sisvitag2.data.repository

import android.util.Log
import com.example.sisvitag2.data.model.Pregunta
import com.example.sisvitag2.data.model.Respuesta
import com.example.sisvitag2.data.model.Test
import com.example.sisvitag2.data.model.TestSubmission
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.tasks.await
import java.util.concurrent.ConcurrentHashMap

class TestRepository(
    private val firestore: FirebaseFirestore,
    private val functions: FirebaseFunctions
) {

    companion object {
        private const val TAG = "TestRepository"
        // Nombres de colecciones en Firestore
        private const val TESTS_COLLECTION = "tests"
        private const val PREGUNTAS_COLLECTION = "preguntas"
        private const val RESPUESTAS_COLLECTION = "respuestas"
        // Nombre de la Cloud Function para enviar resultados
        private const val SUBMIT_TEST_FUNCTION = "submitTestResults"
    }

    // Firestore tiene caché offline, pero esto evita lecturas repetidas en la misma sesión.
    // Usar ConcurrentHashMap para seguridad en hilos, aunque con coroutines no es estrictamente necesario.
    @Volatile private var cachedTests: List<Test>? = null
    private val cachedPreguntas = ConcurrentHashMap<String, List<Pregunta>>()
    private val cachedRespuestas = ConcurrentHashMap<String, List<Respuesta>>()
    // ----------------------------------------------------------------

    /**
     * Obtiene la lista de todos los tests disponibles desde Firestore, usando caché en memoria.
     */
    suspend fun getTests(): List<Test> {
        cachedTests?.let {
            Log.d(TAG, "Retornando tests desde caché en memoria.")
            return it
        }

        Log.d(TAG, "Obteniendo tests desde Firestore.")
        return try {
            val snapshot = firestore.collection(TESTS_COLLECTION)
                // Opcional: Ordenar por nombre o algún otro campo
                // .orderBy("nombre", Query.Direction.ASCENDING)
                .get().await()

            // Deserializa la lista de documentos a objetos Test (adaptados con ID String, etc.)
            val tests = snapshot.toObjects<Test>()
            cachedTests = tests // Guarda en caché
            Log.d(TAG, "Tests obtenidos de Firestore: ${tests.size} encontrados.")
            tests
        } catch (e: Exception) {
            Log.e(TAG, "Error obteniendo tests desde Firestore", e)
            emptyList() // Devuelve lista vacía en caso de error
        }
    }

    /**
     * Obtiene las preguntas para un test específico (por ID de documento), usando caché.
     * @param testId El ID del documento del test en Firestore.
     */
    suspend fun getPreguntas(testId: String): List<Pregunta> {
        if (testId.isBlank()) return emptyList()

        cachedPreguntas[testId]?.let {
            Log.d(TAG, "Retornando preguntas para test $testId desde caché.")
            return it
        }

        Log.d(TAG, "Obteniendo preguntas para test $testId desde Firestore.")
        return try {
            val snapshot = firestore.collection(PREGUNTAS_COLLECTION)
                .whereEqualTo("testId", testId) // Filtra por el ID del test (campo String)
                .orderBy("numeroPregunta", Query.Direction.ASCENDING) // Ordena por número
                .get().await()

            val preguntas = snapshot.toObjects<Pregunta>()
            cachedPreguntas[testId] = preguntas // Guarda en caché
            Log.d(TAG, "Preguntas obtenidas para test $testId: ${preguntas.size} encontradas.")
            preguntas
        } catch (e: Exception) {
            Log.e(TAG, "Error obteniendo preguntas para test $testId", e)
            emptyList()
        }
    }

    /**
     * Obtiene las opciones de respuesta para un test específico (por ID de documento), usando caché.
     * @param testId El ID del documento del test en Firestore.
     */
    suspend fun getRespuestas(testId: String): List<Respuesta> {
        if (testId.isBlank()) return emptyList()

        cachedRespuestas[testId]?.let {
            Log.d(TAG, "Retornando respuestas para test $testId desde caché.")
            return it
        }

        Log.d(TAG, "Obteniendo respuestas para test $testId desde Firestore.")
        return try {
            val snapshot = firestore.collection(RESPUESTAS_COLLECTION)
                .whereEqualTo("testId", testId) // Filtra por el ID del test (campo String)
                .orderBy("numeroRespuesta", Query.Direction.ASCENDING) // Ordena por valor/número
                .get().await()

            val respuestas = snapshot.toObjects<Respuesta>()
            cachedRespuestas[testId] = respuestas // Guarda en caché
            Log.d(TAG, "Respuestas obtenidas para test $testId: ${respuestas.size} encontradas.")
            respuestas
        } catch (e: Exception) {
            Log.e(TAG, "Error obteniendo respuestas para test $testId", e)
            emptyList()
        }
    }

    /**
     * Envía los resultados de un test completado a una Cloud Function para procesamiento.
     *
     * @param testSubmission Objeto que contiene el ID del test y las respuestas del usuario
     *                       (asegúrate de que use IDs de String como definimos en el modelo adaptado).
     * @return Un SubmitTestResult indicando éxito (con diagnóstico/puntaje si la función los devuelve) o el tipo de error.
     */
    suspend fun submitTest(testSubmission: TestSubmission): SubmitTestResult {
        // 1. Prepara los datos para la Cloud Function
        // Convierte el objeto TestSubmission a un Map que la función pueda entender.
        // El UID del usuario se obtiene implícitamente en la función.
        val data = mapOf(
            "testId" to testSubmission.testId,
            "respuestas" to testSubmission.respuestas.map { mapOf("preguntaId" to it.preguntaId, "respuestaId" to it.respuestaId) }
        )

        Log.d(TAG, "Llamando a Cloud Function: $SUBMIT_TEST_FUNCTION con datos: $data")

        return try {
            // 2. Llama a la Cloud Function (HTTPS Callable)
            val result = functions
                .getHttpsCallable(SUBMIT_TEST_FUNCTION)
                .call(data)
                .await()

            // 3. Procesa la respuesta de la Cloud Function
            val resultMap = result.data as? Map<String, Any>
            if (resultMap != null) {
                Log.d(TAG, "Respuesta recibida de $SUBMIT_TEST_FUNCTION: $resultMap")
                // Extrae diagnóstico y puntaje (si la función los devuelve)
                val diagnostico = resultMap["diagnostico"] as? String
                val puntaje = (resultMap["puntaje"] as? Number)?.toInt() // Convierte Number a Int

                // --- NO actualizar UserSession ---
                // El ViewModel que llame a esto recibirá el resultado
                // y decidirá cómo mostrarlo o almacenarlo temporalmente.

                SubmitTestResult.Success(diagnostico, puntaje)

            } else {
                Log.e(TAG, "Respuesta de $SUBMIT_TEST_FUNCTION no es un mapa válido: ${result.data}")
                SubmitTestResult.Failure(SubmitTestError.INVALID_RESPONSE, "Respuesta inesperada del servidor.")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error llamando a Cloud Function $SUBMIT_TEST_FUNCTION", e)
            // Aquí podrías intentar mapear 'e' a errores más específicos si es una HttpsException
            SubmitTestResult.Failure(SubmitTestError.FUNCTION_CALL_FAILED, e.message)
        }
    }

    /**
     * Limpia las cachés en memoria. Puede ser útil al hacer logout o refrescar datos manualmente.
     */
    fun clearCache() {
        cachedTests = null
        cachedPreguntas.clear()
        cachedRespuestas.clear()
        Log.d(TAG, "Cachés de TestRepository limpiadas.")
    }
}

// --- Clases auxiliares para manejar el resultado del envío del test ---

sealed class SubmitTestResult {
    data class Success(val diagnostico: String?, val puntaje: Int?) : SubmitTestResult()
    data class Failure(val errorType: SubmitTestError, val message: String? = null) : SubmitTestResult()
}

enum class SubmitTestError {
    ALL_QUESTIONS_NOT_ANSWERED, // <--- AÑADIDO
    INVALID_RESPONSE, // La función devolvió algo inesperado
    FUNCTION_CALL_FAILED, // Error de red, función no encontrada, error interno de la función
    UNKNOWN
}