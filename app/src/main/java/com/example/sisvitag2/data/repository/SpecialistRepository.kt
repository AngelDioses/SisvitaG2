package com.example.sisvitag2.data.repository

import com.example.sisvitag2.data.model.SpecialistFeedback
import com.example.sisvitag2.data.model.SpecialistTestSubmission
import com.example.sisvitag2.data.model.UserProfileData
import com.example.sisvitag2.data.model.EmotionalAnalysisSubmission
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.util.*

class SpecialistRepository(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    
    // Obtener datos del especialista actual
    suspend fun getSpecialistData(): UserProfileData? {
        return try {
            val userId = auth.currentUser?.uid ?: return null
            val document = firestore.collection("usuarios").document(userId).get().await()
            
            if (document.exists()) {
                document.toObject(UserProfileData::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            throw Exception("Error al obtener datos del especialista: ${e.message}")
        }
    }
    
    // Obtener cantidad de tests pendientes
    suspend fun getPendingTestsCount(): Int {
        return try {
            val snapshot = firestore.collection("test_submissions")
                .whereEqualTo("status", "PENDING")
                .get()
                .await()
            
            snapshot.size()
        } catch (e: Exception) {
            throw Exception("Error al obtener tests pendientes: ${e.message}")
        }
    }
    
    // Obtener cantidad de tests completados hoy
    suspend fun getCompletedTodayCount(): Int {
        return try {
            val today = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            
            val snapshot = firestore.collection("specialist_feedback")
                .whereEqualTo("specialistId", auth.currentUser?.uid)
                .whereGreaterThanOrEqualTo("feedbackDate", today.time)
                .get()
                .await()
            
            snapshot.size()
        } catch (e: Exception) {
            throw Exception("Error al obtener tests completados hoy: ${e.message}")
        }
    }
    
    // Obtener lista de tests pendientes
    suspend fun getPendingTests(): List<SpecialistTestSubmission> {
        return try {
            val snapshot = firestore.collection("test_submissions")
                .whereEqualTo("status", "PENDING")
                .orderBy("submissionDate", Query.Direction.DESCENDING)
                .get()
                .await()
            android.util.Log.d("SpecialistRepository", "Tests pendientes encontrados: ${snapshot.size()}")
            val result = snapshot.documents.mapNotNull { doc ->
                val obj = doc.toObject(SpecialistTestSubmission::class.java)
                if (obj == null) {
                    android.util.Log.e("SpecialistRepository", "Error al convertir documento a SpecialistTestSubmission: ${doc.id}")
                }
                obj?.copy(id = doc.id)
            }
            android.util.Log.d("SpecialistRepository", "Tests convertidos correctamente: ${result.size}")
            result
        } catch (e: Exception) {
            android.util.Log.e("SpecialistRepository", "Error al obtener tests pendientes: ${e.message}")
            throw Exception("Error al obtener tests pendientes: ${e.message}")
        }
    }
    
    // Obtener test específico por ID
    suspend fun getTestById(testId: String): SpecialistTestSubmission? {
        return try {
            val document = firestore.collection("test_submissions").document(testId).get().await()
            
            if (document.exists()) {
                document.toObject(SpecialistTestSubmission::class.java)?.copy(id = document.id)
            } else {
                null
            }
        } catch (e: Exception) {
            throw Exception("Error al obtener test: ${e.message}")
        }
    }
    
    // Enviar feedback del especialista
    suspend fun sendFeedback(feedback: SpecialistFeedback): String {
        return try {
            val feedbackWithId = feedback.copy(
                id = firestore.collection("specialist_feedback").document().id,
                specialistId = auth.currentUser?.uid ?: throw Exception("Usuario no autenticado")
            )
            
            // Guardar feedback
            firestore.collection("specialist_feedback")
                .document(feedbackWithId.id)
                .set(feedbackWithId)
                .await()
            
            // Actualizar estado del test
            firestore.collection("test_submissions")
                .document(feedback.testSubmissionId)
                .update(
                    mapOf(
                        "status" to "COMPLETED",
                        "feedbackId" to feedbackWithId.id,
                        "specialistId" to feedbackWithId.specialistId,
                        "specialistName" to feedbackWithId.specialistName
                    )
                )
                .await()
            
            feedbackWithId.id
        } catch (e: Exception) {
            throw Exception("Error al enviar feedback: ${e.message}")
        }
    }
    
    // Obtener historial de feedback enviado
    suspend fun getFeedbackHistory(): List<SpecialistFeedback> {
        return try {
            val specialistId = auth.currentUser?.uid ?: return emptyList()
            
            val snapshot = firestore.collection("specialist_feedback")
                .whereEqualTo("specialistId", specialistId)
                .orderBy("feedbackDate", Query.Direction.DESCENDING)
                .get()
                .await()
            
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(SpecialistFeedback::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            throw Exception("Error al obtener historial de feedback: ${e.message}")
        }
    }

    suspend fun getCurrentSpecialistName(): String {
        val userId = auth.currentUser?.uid ?: return ""
        val document = firestore.collection("usuarios").document(userId).get().await()
        val nombre = document.getString("nombre") ?: ""
        val apellido = document.getString("apellidopaterno") ?: ""
        val resultado = "$nombre $apellido".trim()
        android.util.Log.d("SpecialistRepository", "getCurrentSpecialistName: uid=$userId, nombre='$nombre', apellido='$apellido', resultado='$resultado'")
        return resultado
    }

    fun getCurrentSpecialistUid(): String? {
        return auth.currentUser?.uid
    }

    // Obtener lista de análisis emocionales pendientes
    suspend fun getPendingEmotionalAnalyses(): List<EmotionalAnalysisSubmission> {
        return try {
            val snapshot = firestore.collection("emotional_analysis_submissions")
                .whereEqualTo("status", "pending")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()
            android.util.Log.d("SpecialistRepository", "Análisis emocionales pendientes encontrados: ${snapshot.size()}")
            val result = snapshot.documents.mapNotNull { doc ->
                val obj = doc.toObject(EmotionalAnalysisSubmission::class.java)
                if (obj == null) {
                    android.util.Log.e("SpecialistRepository", "Error al convertir documento a EmotionalAnalysisSubmission: ${doc.id}")
                }
                obj?.copy(id = doc.id)
            }
            android.util.Log.d("SpecialistRepository", "Análisis convertidos correctamente: ${result.size}")
            result
        } catch (e: Exception) {
            android.util.Log.e("SpecialistRepository", "Error al obtener análisis emocionales pendientes: ${e.message}")
            throw Exception("Error al obtener análisis emocionales pendientes: ${e.message}")
        }
    }

    // Guardar feedback de análisis emocional
    suspend fun sendEmotionalAnalysisFeedback(
        analysisId: String,
        userId: String,
        userName: String,
        apellidoPaterno: String,
        fechaNacimiento: String,
        recommendations: String,
        severity: String
    ) {
        val feedbackData = mapOf(
            "analysisId" to analysisId,
            "userId" to userId,
            "userName" to userName,
            "apellidoPaterno" to apellidoPaterno,
            "fechaNacimiento" to fechaNacimiento,
            "timestamp" to com.google.firebase.Timestamp.now(),
            "recommendations" to recommendations,
            "severity" to severity,
            "status" to "reviewed"
        )
        val feedbackRef = firestore.collection("feedback_emotional_analysis").document()
        feedbackRef.set(feedbackData).await()
        // Marcar submission como revisado
        firestore.collection("emotional_analysis_submissions")
            .document(analysisId)
            .update(mapOf("status" to "reviewed", "feedbackId" to feedbackRef.id)).await()
    }
} 