package com.example.sisvitag2.data.repository

import com.example.sisvitag2.data.model.SpecialistFeedback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class FeedbackRepository(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    suspend fun getReceivedFeedbacks(): List<SpecialistFeedback> {
        return try {
            val userId = auth.currentUser?.uid ?: return emptyList()
            val snapshot = firestore.collection("specialist_feedback")
                .whereEqualTo("userId", userId)
                .orderBy("feedbackDate", Query.Direction.DESCENDING)
                .get()
                .await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(SpecialistFeedback::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            throw Exception("Error al obtener feedbacks recibidos: ${e.message}")
        }
    }
} 