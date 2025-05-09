package com.example.sisvitag2.ui.screens.testForm

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.sisvitag2.data.model.Pregunta
import com.example.sisvitag2.data.model.Respuesta
import com.example.sisvitag2.data.model.RespuestaSubmission
import com.example.sisvitag2.data.model.TestSubmission
import com.example.sisvitag2.ui.screens.test.TestViewModel
import com.example.sisvitag2.data.repository.SubmitTestResult
import com.example.sisvitag2.data.repository.SubmitTestError
import com.example.sisvitag2.ui.theme.SisvitaG2Theme
import org.koin.androidx.compose.koinViewModel

@Composable
fun TestFormScreen(
    navController: NavController,
    testId: String?, // Recibe el testId de la ruta
    viewModel: TestViewModel = koinViewModel() // Usar TestViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val selectedAnswers = remember { mutableStateMapOf<String, String>() }

    var showResultDialog by remember { mutableStateOf<SubmitTestResult.Success?>(null) }
    var showErrorDialog by remember { mutableStateOf<SubmitTestResult.Failure?>(null) }

    // Cargar preguntas y respuestas cuando testId cambie o la pantalla se cree con un testId
    LaunchedEffect(testId) {
        if (testId != null) {
            Log.d("TestFormScreen", "TestId no nulo: $testId. Llamando a selectTestAndLoadDetails.")
            viewModel.selectTestAndLoadDetails(testId)
        } else {
            if (uiState.selectedTestId == null) {
                Log.w("TestFormScreen", "testId es nulo y no hay test seleccionado en ViewModel.")
            } else {
                Log.d("TestFormScreen", "testId de ruta es nulo, pero selectedTestId en VM es ${uiState.selectedTestId}")
            }
        }
    }

    LaunchedEffect(uiState.submitResult) {
        when (val result = uiState.submitResult) {
            is SubmitTestResult.Success -> {
                showResultDialog = result; showErrorDialog = null
            }
            is SubmitTestResult.Failure -> {
                showErrorDialog = result; showResultDialog = null
            }
            null -> {
                showResultDialog = null; showErrorDialog = null
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp) // Ejemplo de padding superior
    ) {
        when {
            uiState.isLoadingPreguntasRespuestas -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            uiState.selectedTestId == null -> {
                Text(
                    text = "Por favor, seleccione un test para continuar.",
                    modifier = Modifier.align(Alignment.Center).padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }
            uiState.preguntas.isEmpty() && !uiState.isLoadingPreguntasRespuestas -> {
                Text(
                    text = "No se encontraron preguntas para el test seleccionado.",
                    modifier = Modifier.align(Alignment.Center).padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }
            else -> {
                TestFormContent(
                    // paddingValues ya no se pasa
                    preguntas = uiState.preguntas,
                    respuestasDisponibles = uiState.respuestas,
                    selectedAnswers = selectedAnswers,
                    onSubmit = { respuestasSeleccionadas ->
                        val currentTestId = uiState.selectedTestId
                        if (currentTestId != null) {
                            val testSubmission = TestSubmission(
                                testId = currentTestId,
                                respuestas = respuestasSeleccionadas
                            )
                            viewModel.submitTest(testSubmission)
                        } else {
                            Toast.makeText(context, "Error: No hay un test seleccionado.", Toast.LENGTH_LONG).show()
                        }
                    }
                )
            }
        }

        if (uiState.isSubmitting) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }

    showResultDialog?.let { successResult ->
        ResultDialog(
            diagnostico = successResult.diagnostico ?: "No disponible",
            puntaje = successResult.puntaje ?: 0,
            onDismiss = {
                viewModel.clearSubmitResult()
                navController.popBackStack() // Volver a la pantalla anterior (lista de tests o home)
            }
        )
    }

    showErrorDialog?.let { failureResult ->
        ErrorDialog(
            errorMessage = when (failureResult.errorType) {
                SubmitTestError.ALL_QUESTIONS_NOT_ANSWERED -> "Por favor, responda todas las preguntas."
                SubmitTestError.FUNCTION_CALL_FAILED -> "Error de conexión. Intente de nuevo."
                SubmitTestError.INVALID_RESPONSE -> "Respuesta inesperada del servidor."
                else -> failureResult.message ?: "Error desconocido."
            },
            onDismiss = { viewModel.clearSubmitResult() }
        )
    }
}

@Composable
fun TestFormContent(
    // paddingValues: PaddingValues, // ELIMINADO
    preguntas: List<Pregunta>,
    respuestasDisponibles: List<Respuesta>,
    selectedAnswers: MutableMap<String, String>,
    onSubmit: (List<RespuestaSubmission>) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        itemsIndexed(preguntas, key = { _, pregunta -> pregunta.id }) { index, pregunta ->
            QuestionItem(
                index = index,
                pregunta = pregunta,
                respuestas = respuestasDisponibles,
                selectedAnswerId = selectedAnswers[pregunta.id],
                onAnswerSelected = { respuestaId ->
                    selectedAnswers[pregunta.id] = respuestaId
                }
            )
        }
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    if (selectedAnswers.size == preguntas.size && preguntas.isNotEmpty()) {
                        val respuestas = selectedAnswers.map { (preguntaId, respuestaId) ->
                            RespuestaSubmission(preguntaId = preguntaId, respuestaId = respuestaId)
                        }
                        onSubmit(respuestas)
                    } else if (preguntas.isNotEmpty()){
                        Log.w("TestFormContent", "Intento de enviar sin responder todas las preguntas.")
                        // Aquí el ViewModel se encargará de mostrar el error via submitResult
                        // si la validación falla en submitTest().
                        // Alternativamente, podrías mostrar un Toast aquí directamente, pero es mejor centralizar en VM.
                        val respuestas = selectedAnswers.map { (preguntaId, respuestaId) ->
                            RespuestaSubmission(preguntaId = preguntaId, respuestaId = respuestaId)
                        }
                        onSubmit(respuestas) // Dejar que el VM valide
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
                    .height(48.dp),
                enabled = preguntas.isNotEmpty() // Habilitar si hay preguntas, el VM validará si están todas respondidas
            ) {
                Text("Enviar Respuestas", fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun QuestionItem(
    index: Int,
    pregunta: Pregunta,
    respuestas: List<Respuesta>,
    selectedAnswerId: String?,
    onAnswerSelected: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Pregunta ${pregunta.numeroPregunta.takeIf { it > 0 } ?: (index + 1)}: ${pregunta.textoPregunta}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        respuestas.forEach { answer ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onAnswerSelected(answer.id) }
                    .padding(vertical = 4.dp)
            ) {
                RadioButton(
                    selected = (selectedAnswerId == answer.id),
                    onClick = { onAnswerSelected(answer.id) }
                )
                Text(
                    text = answer.textoRespuesta,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

@Composable
fun ResultDialog(
    diagnostico: String,
    puntaje: Int,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Resultado del Test",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Diagnóstico:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = diagnostico,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )
                Text(
                    text = "Puntaje Obtenido:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$puntaje",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("OK")
            }
        },
        dismissButton = null
    )
}

@Composable
fun ErrorDialog(
    errorMessage: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Error al Enviar") },
        text = { Text(text = errorMessage) },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun TestFormContentPreview() {
    val samplePreguntas = listOf(
        Pregunta(id = "p1", testId = "t1", textoPregunta = "¿Te sientes nervioso?", numeroPregunta = 1),
        Pregunta(id = "p2", testId = "t1", textoPregunta = "¿Duermes bien?", numeroPregunta = 2)
    )
    val sampleRespuestas = listOf(
        Respuesta(id = "r1", testId = "t1", textoRespuesta = "Nunca", numeroRespuesta = 0),
        Respuesta(id = "r2", testId = "t1", textoRespuesta = "A veces", numeroRespuesta = 1),
        Respuesta(id = "r3", testId = "t1", textoRespuesta = "Siempre", numeroRespuesta = 2)
    )
    val selectedAnswers = remember { mutableStateMapOf<String, String>() }

    SisvitaG2Theme {
        TestFormContent(
            preguntas = samplePreguntas,
            respuestasDisponibles = sampleRespuestas,
            selectedAnswers = selectedAnswers,
            onSubmit = {}
        )
    }
}