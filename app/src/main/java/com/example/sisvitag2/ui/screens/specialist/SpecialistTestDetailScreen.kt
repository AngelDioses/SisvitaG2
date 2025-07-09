package com.example.sisvitag2.ui.screens.specialist

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sisvitag2.data.model.SpecialistFeedback
import com.example.sisvitag2.data.model.SpecialistTestSubmission
import com.example.sisvitag2.data.model.FeedbackSeverity
import com.example.sisvitag2.data.repository.SpecialistRepository
import com.example.sisvitag2.data.repository.TestRepository
import com.example.sisvitag2.data.model.Pregunta
import com.example.sisvitag2.data.model.Respuesta
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import com.example.sisvitag2.ui.vm.SpecialistViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpecialistTestDetailScreen(
    testId: String,
    onFeedbackSent: () -> Unit = {},
    specialistRepository: SpecialistRepository = koinInject(),
    testRepository: TestRepository = koinInject(), // Inyectar TestRepository
    specialistViewModel: SpecialistViewModel = koinInject() // Inyectar el ViewModel
) {
    var test by remember { mutableStateOf<SpecialistTestSubmission?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    // Estados para datos auxiliares
    var testName by remember { mutableStateOf("") }
    var preguntas by remember { mutableStateOf<List<Pregunta>>(emptyList()) }
    var respuestas by remember { mutableStateOf<List<Respuesta>>(emptyList()) }

    // Feedback form state
    var assessment by remember { mutableStateOf("") }
    var recommendations by remember { mutableStateOf("") }
    var severity by remember { mutableStateOf(FeedbackSeverity.MILD) }
    var isUrgent by remember { mutableStateOf(false) }
    var notes by remember { mutableStateOf("") }
    var sending by remember { mutableStateOf(false) }
    var feedbackSent by remember { mutableStateOf(false) }

    LaunchedEffect(testId) {
        isLoading = true
        error = null
        try {
            test = specialistRepository.getTestById(testId)
            test?.let { t ->
                // Obtener nombre del test
                val testObj = testRepository.getTests().find { it.id == t.testType }
                testName = testObj?.nombre ?: t.testType
                // Obtener preguntas y respuestas
                preguntas = testRepository.getPreguntas(t.testType)
                respuestas = testRepository.getRespuestas(t.testType)
            }
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
    if (test == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No se encontró el test.")
        }
        return
    }

    if (feedbackSent) {
        AlertDialog(
            onDismissRequest = onFeedbackSent,
            title = { Text("Feedback enviado") },
            text = { Text("El feedback fue enviado correctamente.") },
            confirmButton = {
                Button(onClick = onFeedbackSent) { Text("OK") }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Detalle del Test", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text("Usuario: ${test!!.userName}", fontWeight = FontWeight.Medium)
        Text("Tipo de test: $testName")
        Text("Fecha de envío: ${test!!.submissionDate.toDate()}")
        Divider()
        Text("Respuestas:", fontWeight = FontWeight.Bold)
        test!!.answers.forEachIndexed { idx, answer ->
            val pregunta = preguntas.getOrNull(idx)
            val textoPregunta = pregunta?.textoPregunta ?: "Pregunta ${idx + 1}"
            // Buscar la respuesta por ID de respuesta
            val respuesta = respuestas.find { it.id == answer.answerText || it.numeroRespuesta == answer.answer }
            val textoRespuesta = respuesta?.textoRespuesta ?: answer.answerText
            Text("${idx + 1}. $textoPregunta")
            Text("Respuesta: $textoRespuesta", fontSize = 14.sp, modifier = Modifier.padding(start = 8.dp))
        }
        Divider()
        Text("Enviar Feedback", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        OutlinedTextField(
            value = assessment,
            onValueChange = { assessment = it },
            label = { Text("Evaluación del especialista") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = recommendations,
            onValueChange = { recommendations = it },
            label = { Text("Recomendaciones") },
            modifier = Modifier.fillMaxWidth()
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Severidad:", modifier = Modifier.padding(end = 8.dp))
            DropdownMenuBox(severity, onSeverityChange = { severity = it })
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = isUrgent, onCheckedChange = { isUrgent = it })
            Text("¿Es urgente?")
        }
        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Notas adicionales") },
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = {
                sending = true
                scope.launch {
                    try {
                        val specialistName = specialistRepository.getCurrentSpecialistName()
                        android.util.Log.d("SpecialistTestDetailScreen", "Nombre del especialista obtenido antes de enviar feedback: '$specialistName'")
                        val feedback = SpecialistFeedback(
                            testSubmissionId = test!!.id,
                            specialistId = "", // Se completa en el repo
                            specialistName = specialistName, // Guardar nombre correcto
                            userId = test!!.userId,
                            userName = test!!.userName,
                            testType = test!!.testType,
                            feedbackDate = Timestamp.now(),
                            assessment = assessment,
                            recommendations = recommendations.split("\n").filter { it.isNotBlank() },
                            severity = severity,
                            isUrgent = isUrgent,
                            notes = notes
                        )
                        specialistRepository.sendFeedback(feedback)
                        specialistViewModel.loadFeedbackHistory() // Refrescar historial
                        feedbackSent = true
                    } catch (e: Exception) {
                        error = e.message
                    }
                    sending = false
                }
            },
            enabled = !sending && assessment.isNotBlank() && recommendations.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (sending) CircularProgressIndicator(modifier = Modifier.size(20.dp))
            else Text("Enviar Feedback")
        }
    }
}

@Composable
fun DropdownMenuBox(selected: FeedbackSeverity, onSeverityChange: (FeedbackSeverity) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val severidadMap = mapOf(
        FeedbackSeverity.MILD to "Leve",
        FeedbackSeverity.MODERATE to "Moderado",
        FeedbackSeverity.SEVERE to "Severo",
        FeedbackSeverity.CRITICAL to "Crítico"
    )
    Box {
        Button(onClick = { expanded = true }) {
            Text(severidadMap[selected] ?: selected.name)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            FeedbackSeverity.values().forEach { sev ->
                DropdownMenuItem(
                    text = { Text(severidadMap[sev] ?: sev.name) },
                    onClick = {
                        onSeverityChange(sev)
                        expanded = false
                    }
                )
            }
        }
    }
} 