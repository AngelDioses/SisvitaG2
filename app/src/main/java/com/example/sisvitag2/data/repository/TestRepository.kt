package com.example.sisvitag2.data.repository

import android.util.Log
import com.example.sisvitag2.data.model.Pregunta
import com.example.sisvitag2.data.model.Respuesta
import com.example.sisvitag2.data.model.Test
import com.example.sisvitag2.data.model.TestSubmission
import com.example.sisvitag2.data.model.SpecialistTestSubmission
import com.example.sisvitag2.data.model.TestAnswer
import com.example.sisvitag2.data.model.TestStatus
import com.example.sisvitag2.data.model.HistorialItemPaciente
import com.example.sisvitag2.data.model.HistorialTipo
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import kotlinx.coroutines.tasks.await
import java.util.concurrent.ConcurrentHashMap
import com.google.firebase.Timestamp

class TestRepository(
    private val firestore: FirebaseFirestore
) {

    companion object {
        private const val TAG = "TestRepository"
        // Nombres de colecciones en Firestore
        private const val TESTS_COLLECTION = "tests"
        private const val PREGUNTAS_COLLECTION = "preguntas"
        private const val RESPUESTAS_COLLECTION = "respuestas"
        private const val TEST_SUBMISSIONS_COLLECTION = "test_submissions"
        private const val USER_HISTORY_COLLECTION = "user_history"
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
     * Envía los resultados de un test completado directamente a Firestore.
     *
     * @param testSubmission Objeto que contiene el ID del test y las respuestas del usuario
     * @return Un SubmitTestResult indicando éxito o el tipo de error.
     */
    suspend fun submitTest(testSubmission: TestSubmission): SubmitTestResult {
        Log.d(TAG, "Guardando test submission directamente en Firestore: $testSubmission")

        return try {
            // 1. Guardar en test_submissions para el especialista
            val specialistSubmission = SpecialistTestSubmission(
                userId = testSubmission.userId,
                userName = testSubmission.userName,
                testType = testSubmission.testId,
                answers = testSubmission.respuestas.mapIndexed { index, respuesta ->
                    TestAnswer(
                        questionId = index + 1,
                        questionText = "",
                        answer = respuesta.respuestaId.toIntOrNull() ?: 0,
                        answerText = respuesta.respuestaId
                    )
                },
                totalScore = 0,
                submissionDate = Timestamp.now(),
                status = TestStatus.PENDING
            )
            
            firestore.collection(TEST_SUBMISSIONS_COLLECTION)
                .add(specialistSubmission)
                .await()

            // 2. Guardar en user_history para el historial del usuario
            val historyItem = HistorialItemPaciente(
                id = testSubmission.testId,
                tipo = HistorialTipo.TEST_PSICOLOGICO,
                nombreActividad = testSubmission.testName,
                fechaRealizacion = Timestamp.now(),
                tieneFeedback = false
            )
            
            firestore.collection(USER_HISTORY_COLLECTION)
                .document(testSubmission.userId)
                .collection("tests")
                .add(historyItem)
                .await()

            Log.d(TAG, "Test submission guardado exitosamente en Firestore")
            SubmitTestResult.Success(null, null)

        } catch (e: Exception) {
            Log.e(TAG, "Error guardando test submission en Firestore", e)
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