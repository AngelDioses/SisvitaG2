package com.example.sisvitag2.ui.screens.specialist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sisvitag2.data.model.EmotionalAnalysisSubmission
import com.example.sisvitag2.data.model.EmotionalAnalysisFeedback
import com.example.sisvitag2.data.repository.emotionalAnalysis.EmotionalAnalysisRepository
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.java.KoinJavaComponent

@Composable
fun EmotionalAnalysisPendingScreen(
    repository: EmotionalAnalysisRepository = KoinJavaComponent.getKoin().get(),
    specialistName: String = "Especialista"
) {
    var pendingAnalyses by remember { mutableStateOf<List<EmotionalAnalysisSubmission>>(emptyList()) }
    var selectedAnalysis by remember { mutableStateOf<EmotionalAnalysisSubmission?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        isLoading = true
        errorMsg = null
        try {
            pendingAnalyses = repository.getPendingEmotionalAnalyses()
        } catch (e: Exception) {
            errorMsg = e.message
        }
        isLoading = false
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            errorMsg != null -> Text(errorMsg ?: "Error", color = MaterialTheme.colorScheme.error, modifier = Modifier.align(Alignment.Center))
            pendingAnalyses.isEmpty() -> Text("No hay análisis emocionales pendientes.", modifier = Modifier.align(Alignment.Center))
            else -> LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                items(pendingAnalyses) { analysis ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable { selectedAnalysis = analysis },
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Usuario: ${analysis.userName}", fontWeight = FontWeight.Bold)
                            Text("Fecha: ${analysis.timestamp.toDate()}", fontSize = 12.sp)
                            Text("Estado: ${analysis.status}", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
        selectedAnalysis?.let { analysis ->
            EmotionalAnalysisDetailDialog(
                analysis = analysis,
                onDismiss = { selectedAnalysis = null },
                onSendFeedback = { feedbackText, notes ->
                    scope.launch {
                        val specialistId = "" // Aquí deberías obtener el UID del especialista autenticado
                        val feedback = EmotionalAnalysisFeedback(
                            submissionId = analysis.id,
                            specialistId = specialistId,
                            specialistName = specialistName,
                            userId = analysis.userId,
                            recommendation = feedbackText,
                            notes = notes
                        )
                        repository.sendEmotionalAnalysisFeedback(feedback)
                        // Actualizar lista tras enviar feedback
                        pendingAnalyses = repository.getPendingEmotionalAnalyses()
                        selectedAnalysis = null
                    }
                }
            )
        }
    }
}

@Composable
fun EmotionalAnalysisDetailDialog(
    analysis: EmotionalAnalysisSubmission,
    onDismiss: () -> Unit,
    onSendFeedback: (String, String) -> Unit
) {
    var feedbackText by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var sending by remember { mutableStateOf(false) }
    var sent by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Análisis Emocional de ${analysis.userName}") },
        text = {
            Column {
                Text("Resultados:")
                analysis.results.forEach { (emotion, value) ->
                    Text("$emotion: $value")
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text("Recomendación del especialista:")
                OutlinedTextField(
                    value = feedbackText,
                    onValueChange = { feedbackText = it },
                    label = { Text("Recomendación") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notas (opcional)") },
                    modifier = Modifier.fillMaxWidth()
                )
                errorMsg?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                if (sent) Text("¡Feedback enviado!", color = MaterialTheme.colorScheme.primary)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (feedbackText.isBlank()) {
                        errorMsg = "La recomendación no puede estar vacía."
                        return@Button
                    }
                    sending = true
                    errorMsg = null
                    onSendFeedback(feedbackText, notes)
                    sending = false
                    sent = true
                },
                enabled = !sending && !sent
            ) {
                if (sending) CircularProgressIndicator(modifier = Modifier.size(20.dp))
                else Text(if (sent) "Enviado" else "Enviar Feedback")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cerrar") }
        }
    )
} 