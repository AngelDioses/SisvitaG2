package com.example.sisvitag2.ui.screens.specialist

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.sisvitag2.data.model.EmotionalAnalysisSubmission
import com.example.sisvitag2.data.repository.SpecialistRepository
import com.example.sisvitag2.data.repository.emotionalAnalysis.EmotionalAnalysisResponse
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import com.example.sisvitag2.ui.vm.SpecialistViewModel
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import com.example.sisvitag2.data.model.EmotionalAnalysisFeedback
import com.example.sisvitag2.data.repository.emotionalAnalysis.EmotionalAnalysisRepository
import com.google.firebase.auth.FirebaseAuth

enum class EmotionalSeverity(val label: String) {
    MILD("Leve"), MODERATE("Moderado"), SEVERE("Severo"), CRITICAL("Crítico")
}

@Composable
fun EmotionalAnalysisDetailScreen(
    analysisId: String,
    navController: NavController,
    specialistRepository: SpecialistRepository = koinInject(),
    specialistViewModel: SpecialistViewModel = koinViewModel()
) {
    var analysis by remember { mutableStateOf<EmotionalAnalysisSubmission?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    // Feedback form state
    var recommendations by remember { mutableStateOf("") }
    var severity by remember { mutableStateOf(EmotionalSeverity.MILD) }
    var sending by remember { mutableStateOf(false) }
    var feedbackSent by remember { mutableStateOf(false) }

    val emotionalAnalysisRepository: EmotionalAnalysisRepository = koinInject()
    val currentUser = FirebaseAuth.getInstance().currentUser

    // Cargar el análisis por ID
    LaunchedEffect(analysisId) {
        isLoading = true
        error = null
        try {
            val analyses = specialistRepository.getPendingEmotionalAnalyses()
            analysis = analyses.find { it.id == analysisId }
        } catch (e: Exception) {
            error = e.message
        }
        isLoading = false
    }

    if (isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }
    if (error != null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Error: $error")
        }
        return
    }
    if (analysis == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No se encontró el análisis.")
        }
        return
    }

    if (feedbackSent) {
        AlertDialog(
            onDismissRequest = { navController.popBackStack() },
            title = { Text("Feedback enviado") },
            text = { Text("El feedback fue enviado correctamente.") },
            confirmButton = {
                Button(onClick = { navController.popBackStack() }) { Text("OK") }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Detalle de Análisis Emocional", style = MaterialTheme.typography.titleLarge)
        Text("Usuario: ${analysis!!.userName} ${analysis!!.apellidoPaterno}", fontWeight = androidx.compose.ui.text.font.FontWeight.Medium)
        Text("Fecha de nacimiento: ${analysis!!.fechaNacimiento}")
        Text("Enviado: ${analysis!!.timestamp.toDate()}")
        Divider()
        // Mostrar gráfico de barras
        EmotionalBarChart(results = analysis!!.results)
        Divider()
        Text("Enviar Feedback", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(
            value = recommendations,
            onValueChange = { recommendations = it },
            label = { Text("Recomendaciones") },
            modifier = Modifier.fillMaxWidth()
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Severidad:", modifier = Modifier.padding(end = 8.dp))
            EmotionalSeverityDropdown(severity, onSeverityChange = { severity = it })
        }
        Button(
            onClick = {
                sending = true
                scope.launch {
                    try {
                        val feedback = EmotionalAnalysisFeedback(
                            submissionId = analysis!!.id,
                            specialistId = currentUser?.uid ?: "",
                            specialistName = currentUser?.displayName ?: "",
                            userId = analysis!!.userId,
                            feedbackDate = com.google.firebase.Timestamp.now(),
                            recommendation = recommendations,
                            notes = severity.label
                        )
                        emotionalAnalysisRepository.sendEmotionalAnalysisFeedback(feedback)
                        feedbackSent = true
                        specialistViewModel.loadPendingEmotionalAnalyses()
                    } catch (e: Exception) {
                        error = e.message
                    }
                    sending = false
                }
            },
            enabled = !sending && recommendations.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (sending) CircularProgressIndicator(modifier = Modifier.size(20.dp))
            else Text("Enviar Feedback")
        }
    }
}

@Composable
fun EmotionalBarChart(results: Map<String, Int>) {
    val total = results.values.sum().takeIf { it > 0 } ?: 1
    Column(modifier = Modifier.fillMaxWidth()) {
        results.forEach { (emotion, value) ->
            val percentage = (value.toFloat() / total * 100)
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(emotion.replaceFirstChar { it.uppercase() }, modifier = Modifier.width(90.dp))
                LinearProgressIndicator(
                    progress = percentage / 100f,
                    modifier = Modifier.weight(1f).height(8.dp).padding(horizontal = 8.dp)
                )
                Text("${String.format("%.1f", percentage)}%", modifier = Modifier.width(48.dp))
            }
        }
    }
}

@Composable
fun EmotionalSeverityDropdown(selected: EmotionalSeverity, onSeverityChange: (EmotionalSeverity) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Button(onClick = { expanded = true }) {
            Text(selected.label)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            EmotionalSeverity.values().forEach { sev ->
                DropdownMenuItem(
                    text = { Text(sev.label) },
                    onClick = {
                        onSeverityChange(sev)
                        expanded = false
                    }
                )
            }
        }
    }
} 