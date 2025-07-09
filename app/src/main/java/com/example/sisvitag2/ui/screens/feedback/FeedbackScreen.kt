package com.example.sisvitag2.ui.screens.feedback

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sisvitag2.data.model.SpecialistFeedback
import com.example.sisvitag2.ui.vm.FeedbackUiState
import com.example.sisvitag2.ui.vm.FeedbackViewModel
import org.koin.androidx.compose.koinViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import android.util.Log

fun getSeverityText(severity: String): String = when (severity) {
    "MILD" -> "Leve"
    "MODERATE" -> "Moderado"
    "SEVERE" -> "Severo"
    "CRITICAL" -> "Crítico"
    else -> severity
}

@Composable
fun FeedbackScreen(
    viewModel: FeedbackViewModel = koinViewModel(),
) {
    Log.d("FeedbackScreen", "FeedbackScreen renderizado")
    LaunchedEffect(Unit) {
        viewModel.loadFeedbacks()
    }
    val uiState by viewModel.uiState.collectAsState()
    var selectedFeedback by remember { mutableStateOf<SpecialistFeedback?>(null) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var testNames by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    // Obtener nombres de tests al cargar feedbacks
    LaunchedEffect(uiState) {
        if (uiState is FeedbackUiState.Success) {
            val feedbacks = (uiState as FeedbackUiState.Success).feedbacks
            val firestore = FirebaseFirestore.getInstance()
            val testIds = feedbacks.map { it.testType }.distinct()
            val namesMap = mutableMapOf<String, String>()
            testIds.forEach { testId ->
                firestore.collection("tests").document(testId).get()
                    .addOnSuccessListener { doc ->
                        doc.getString("nombre")?.let { name ->
                            namesMap[testId] = name
                            testNames = namesMap.toMap()
                        }
                    }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (uiState) {
            is FeedbackUiState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            is FeedbackUiState.Error -> {
                Text(
                    text = (uiState as FeedbackUiState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            is FeedbackUiState.Success -> {
                val feedbacks = (uiState as FeedbackUiState.Success).feedbacks
                if (feedbacks.isEmpty()) {
                    Text(
                        text = "No tienes feedbacks recibidos aún.",
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                        items(feedbacks) { feedback ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    .clickable { selectedFeedback = feedback },
                                elevation = CardDefaults.cardElevation(4.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(text = "De: ${if (feedback.specialistName.isNotBlank()) feedback.specialistName else feedback.userName}", style = MaterialTheme.typography.titleMedium)
                                    Text(text = "Test: ${testNames[feedback.testType] ?: feedback.testType}", style = MaterialTheme.typography.bodyMedium)
                                    Text(text = "Fecha: ${feedback.feedbackDate.toDate()}", style = MaterialTheme.typography.bodySmall)
                                    Text(text = "Severidad: ${getSeverityText(feedback.severity.name)}", style = MaterialTheme.typography.bodySmall)
                                    if (feedback.isUrgent) {
                                        Text(text = "¡Urgente!", color = MaterialTheme.colorScheme.error)
                                    }
                                    Text(text = "Análisis: ${feedback.assessment}", style = MaterialTheme.typography.bodyMedium)
                                    if (feedback.recommendations.isNotEmpty()) {
                                        Text(text = "Recomendaciones:", style = MaterialTheme.typography.bodySmall)
                                        feedback.recommendations.forEach { rec ->
                                            Text(text = "- $rec", style = MaterialTheme.typography.bodySmall)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Diálogo de detalles
        selectedFeedback?.let { feedback ->
            AlertDialog(
                onDismissRequest = { selectedFeedback = null },
                title = { Text("Detalles del Feedback") },
                text = {
                    Column {
                        Text("Especialista: ${if (feedback.specialistName.isNotBlank()) feedback.specialistName else feedback.userName}")
                        Text("Test: ${testNames[feedback.testType] ?: feedback.testType}")
                        Text("Fecha: ${feedback.feedbackDate.toDate()}")
                        Text("Evaluación: ${feedback.assessment}")
                        Text("Recomendaciones:")
                        feedback.recommendations.forEach { rec ->
                            Text("- $rec")
                        }
                        Text("Notas: ${feedback.notes}")
                        Text("Severidad: ${getSeverityText(feedback.severity.name)}")
                        if (feedback.isUrgent) Text("¡Atención urgente!", color = MaterialTheme.colorScheme.error)
                        feedback.followUpDate?.let {
                            Text("Fecha de seguimiento: ${it.toDate()}")
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { selectedFeedback = null }) {
                        Text("Cerrar")
                    }
                }
            )
        }
    }
} 