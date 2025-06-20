package com.example.sisvitag2.data.repository

import android.util.Log
import com.example.sisvitag2.data.model.FeedbackDetallado
import com.example.sisvitag2.data.model.HistorialItemPaciente
import com.example.sisvitag2.data.model.HistorialTipo
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.Date

class HistoryRepository(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    companion object {
        private const val TAG = "HistoryRepository"
        private const val DIAGNOSTICOS_COLLECTION = "diagnosticos"
        private const val ANALISIS_RESULTADOS_COLLECTION = "analisisResultados"
        private const val OBSERVACIONES_COLLECTION = "observaciones"
        private const val TESTS_COLLECTION = "tests"
        private const val PUNTUACIONES_COLLECTION = "puntuaciones"
    }

    suspend fun getHistorialPaciente(lastDays: Int = 30): Result<List<HistorialItemPaciente>> {
        val currentUser = auth.currentUser ?: return Result.failure(Exception("Usuario no autenticado."))
        val userId = currentUser.uid

        return try {
            Log.d(TAG, "Obteniendo historial para $userId, últimos $lastDays días.")
            val calendar = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -lastDays) }
            val startDateTimestamp = Timestamp(calendar.time)

            coroutineScope {
                // Obtener diagnósticos de tests psicológicos
                val testsAsync = async {
                    val diagnosticosSnapshot = firestore.collection(DIAGNOSTICOS_COLLECTION)
                        .whereEqualTo("personaId", userId)
                        .whereGreaterThanOrEqualTo("fecha", startDateTimestamp)
                        .orderBy("fecha", Query.Direction.DESCENDING)
                        .get().await()

                    diagnosticosSnapshot.documents.mapNotNull { doc ->
                        val diagnosticoId = doc.id
                        val testId = doc.getString("testId")
                        val fecha = doc.getTimestamp("fecha") ?: Timestamp.now()
                        val puntaje = (doc.getLong("puntaje") ?: 0L).toInt()
                        val puntuacionId = doc.getString("puntuacionId")

                        var testNombre = "Test Desconocido"
                        if (!testId.isNullOrBlank()) {
                            val testDoc = firestore.collection(TESTS_COLLECTION).document(testId).get().await()
                            testNombre = testDoc.getString("nombre") ?: testNombre
                        }

                        var diagnosticoTextoBreve = "Resultado no disponible"
                        if (!puntuacionId.isNullOrBlank()){
                            val puntuacionDoc = firestore.collection(PUNTUACIONES_COLLECTION).document(puntuacionId).get().await()
                            diagnosticoTextoBreve = puntuacionDoc.getString("diagnostico") ?: diagnosticoTextoBreve
                        }

                        val observacionSnapshot = firestore.collection(OBSERVACIONES_COLLECTION)
                            .whereEqualTo("itemId", diagnosticoId)
                            .whereEqualTo("itemType", "TEST_PSICOLOGICO") // Usando el nuevo modelo
                            .whereEqualTo("personaId", userId) // Asegurar que es para este paciente
                            .limit(1).get().await()

                        val tieneFeedback = !observacionSnapshot.isEmpty

                        HistorialItemPaciente(
                            id = diagnosticoId,
                            tipo = HistorialTipo.TEST_PSICOLOGICO,
                            nombreActividad = testNombre,
                            fechaRealizacion = fecha,
                            tieneFeedback = !observacionSnapshot.isEmpty,
                            puntaje = puntaje,
                            diagnosticoTextoBreve = diagnosticoTextoBreve
                        )
                    }
                }

                // Obtener resultados de análisis de video
                val videosAsync = async {
                    val analisisSnapshot = firestore.collection(ANALISIS_RESULTADOS_COLLECTION)
                        .whereEqualTo("userId", userId) // Asumiendo que aquí se guarda el UID del paciente
                        .whereGreaterThanOrEqualTo("timestamp", startDateTimestamp) // Campo timestamp en analisisResultados
                        .whereEqualTo("status", "completado") // Solo los completados
                        .orderBy("timestamp", Query.Direction.DESCENDING)
                        .get().await()

                    analisisSnapshot.documents.mapNotNull { doc ->
                        val videoId = doc.id // El ID del documento de analisisResultados
                        val fecha = doc.getTimestamp("timestamp") ?: Timestamp.now()

                        val observacionSnapshot = firestore.collection(OBSERVACIONES_COLLECTION)
                            .whereEqualTo("itemId", videoId)
                            .whereEqualTo("itemType", "ANALISIS_VIDEO")
                            .whereEqualTo("personaId", userId)
                            .limit(1).get().await()

                        HistorialItemPaciente(
                            id = videoId,
                            tipo = HistorialTipo.ANALISIS_EMOCIONAL_VIDEO,
                            nombreActividad = "Análisis Emocional de Video", // Podrías añadir fecha aquí
                            fechaRealizacion = fecha,
                            tieneFeedback = !observacionSnapshot.isEmpty
                        )
                    }
                }

                val todosLosItems = testsAsync.await() + videosAsync.await()
                val historialOrdenado = todosLosItems.sortedByDescending { it.fechaRealizacion }

                Log.d(TAG, "Historial combinado y ordenado: ${historialOrdenado.size} items.")
                Result.success(historialOrdenado)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error obteniendo historial de paciente.", e)
            Result.failure(e)
        }
    }

    // Nueva función para obtener el feedback detallado de un ítem específico
    suspend fun getFeedbackDetallado(itemId: String, itemType: HistorialTipo, personaId: String): Result<FeedbackDetallado?> {
        return try {
            val observacionSnapshot = firestore.collection(OBSERVACIONES_COLLECTION)
                .whereEqualTo("itemId", itemId)
                .whereEqualTo("itemType", itemType.name) // Comparar con el .name del enum
                .whereEqualTo("personaId", personaId) // Asegurar que es para este paciente
                .orderBy("fechaObservacion", Query.Direction.DESCENDING) // Tomar el más reciente
                .limit(1).get().await()

            if (!observacionSnapshot.isEmpty) {
                val doc = observacionSnapshot.documents.first()
                val feedback = FeedbackDetallado(
                    observacion = doc.getString("observacion"),
                    recomendacion = doc.getString("recomendacion"),
                    fechaFeedback = doc.getTimestamp("fechaObservacion")
                )
                Result.success(feedback)
            } else {
                Result.success(null) // No hay feedback
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error obteniendo feedback detallado para $itemId", e)
            Result.failure(e)
        }
    }
}