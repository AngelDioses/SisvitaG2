package com.example.sisvitag2.ui.screens.results

import android.net.Uri // No parece usarse aquí, considera eliminar si no es necesario
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.sisvitag2.data.repository.emotionalAnalysis.EmotionalAnalysisResponse
import com.example.sisvitag2.ui.theme.SisvitaG2Theme
import com.example.sisvitag2.util.calculateAnxietyLevel // Asumiendo que tienes estos utils
import com.example.sisvitag2.util.mapFirebaseEmotionsToFloatPercentages // Asumiendo que tienes estos utils
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.androidx.compose.koinViewModel
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random // Para el generador de color
import androidx.compose.material3.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.Canvas
import kotlin.math.cos
import kotlin.math.sin
import com.google.firebase.auth.FirebaseAuth
@Composable
fun ResultsScreen(
    // paddingValues: PaddingValues, // ELIMINADO de la firma
    navController: NavController,
    videoId: String?, // Puede ser nulo si hay un error antes de llegar aquí
    viewModel: ResultsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // El ViewModel maneja la carga de datos en su init o con un LaunchedEffect interno
    // si videoId cambia. Si videoId es null al inicio y el estado es Idle,
    // es probable que algo falló antes o la navegación fue incorrecta.
    if (videoId == null && uiState is ResultsUiState.Idle) {
        LaunchedEffect(Unit) {
            Toast.makeText(context, "Error: ID de video no disponible.", Toast.LENGTH_LONG).show()
            Log.e("ResultsScreen", "videoId es nulo y el estado es Idle. Volviendo atrás.")
            navController.popBackStack() // Volver si no hay videoId
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            // El NavHost que contiene esta pantalla (HelpMeNavHost) NO aplica el padding del Scaffold principal.
            // Por lo tanto, esta pantalla maneja su propio padding interno si es necesario.
            .padding(horizontal = 16.dp, vertical = 16.dp) // Padding interno de la pantalla
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Resultados del Análisis",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp) // Espacio debajo del título
        )

        // Mensaje de depuración para ver el estado y los datos
        Text(
            text = "[DEBUG] Estado: ${uiState::class.simpleName} | Emotions: ${if ((uiState as? ResultsUiState.Success)?.emotions != null) "OK" else "NULO"}",
            color = Color.Red,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        when (val state = uiState) {
            is ResultsUiState.Loading -> {
                Spacer(modifier = Modifier.height(64.dp))
                CircularProgressIndicator(modifier = Modifier.size(48.dp))
                Text("Cargando resultados...", modifier = Modifier.padding(top = 16.dp))
            }
            is ResultsUiState.Success -> {
                val emotions = state.emotions
                val anxietyLevel = state.anxietyLevel
                val userName = state.userName // Obtenido por el ViewModel
                // Eliminar: val userId = state.userId

                if (emotions != null) {
                    val emotionPercentages = mapFirebaseEmotionsToFloatPercentages(emotions)

                    ResultCard(emotionPercentages = emotionPercentages)
                    Spacer(modifier = Modifier.height(16.dp))
                    AnxietyLevelCard(level = anxietyLevel)
                    Spacer(modifier = Modifier.height(24.dp))

                    // Agregar los nuevos gráficos  
                    EmotionRadarChart(emotionPercentages = emotionPercentages)  
                    Spacer(modifier = Modifier.height(16.dp))  
                    EmotionalTrendChart(emotionPercentages = emotionPercentages)  
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Recomendaciones",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            try {
                                val emotionsJson = Json.encodeToString(emotions)
                                val encodedEmotions = URLEncoder.encode(emotionsJson, StandardCharsets.UTF_8.toString())
                                navController.navigate("Orientations?emotions=$encodedEmotions")
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error al preparar datos para orientación.", Toast.LENGTH_SHORT).show()
                                Log.e("ResultsScreen", "Error serializando/codificando emociones", e)
                            }
                        }
                    ) {
                        Text(text = "Escuchar las recomendaciones de IA")
                    }

                    // === NUEVO: Botón para enviar al especialista ===
                    Spacer(modifier = Modifier.height(16.dp))
                    var sending by remember { mutableStateOf(false) }
                    var sent by remember { mutableStateOf(false) }
                    var errorMsg by remember { mutableStateOf<String?>(null) }
                    var triggerSend by remember { mutableStateOf(false) }
                    val analysisRepo = org.koin.java.KoinJavaComponent.getKoin().get<com.example.sisvitag2.data.repository.emotionalAnalysis.EmotionalAnalysisRepository>()

                    // Mover LaunchedEffect fuera del callback del botón
                    LaunchedEffect(triggerSend) {
                        if (triggerSend) {
                            sending = true
                            errorMsg = null
                            val resultsMap = mapOf(
                                "angry" to emotions.angry,
                                "disgust" to emotions.disgust,
                                "fear" to emotions.fear,
                                "happy" to emotions.happy,
                                "sad" to emotions.sad,
                                "surprise" to emotions.surprise,
                                "neutral" to emotions.neutral
                            )
                            val currentUser = FirebaseAuth.getInstance().currentUser
                            val userId = currentUser?.uid ?: ""
                            val safeUserName = userName ?: currentUser?.displayName ?: ""
                            val submission = com.example.sisvitag2.data.model.EmotionalAnalysisSubmission(
                                userId = userId,
                                userName = safeUserName,
                                results = resultsMap
                            )
                            val result = analysisRepo.submitEmotionalAnalysis(submission)
                            if (result != null) {
                                sent = true
                            } else {
                                errorMsg = "Error al enviar el análisis. Intenta nuevamente."
                            }
                            sending = false
                            triggerSend = false
                        }
                    }

                    Button(
                        onClick = {
                            if (!sending && !sent) {
                                triggerSend = true
                            }
                        },
                        enabled = !sending && !sent,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (sending) CircularProgressIndicator(modifier = Modifier.size(20.dp))
                        else Text(if (sent) "Enviado" else "Enviar al especialista")
                    }
                    errorMsg?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
                    }
                    if (sent) {
                        Text("¡Análisis enviado correctamente!", color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 8.dp))
                    }
                } else {
                    Text("Datos de emociones no disponibles para mostrar.")
                }
                Spacer(modifier = Modifier.height(16.dp)) // Espacio al final
            }
            is ResultsUiState.Error -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 32.dp).fillMaxWidth()
                ) {
                    Text("Error al Cargar Resultados", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(state.message, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = {
                        // Reintentar solo si hay un videoId válido para reintentar
                        if (videoId != null) {
                            viewModel.retryLoadResults() // ViewModel debería usar el videoId que ya tiene
                        } else {
                            Toast.makeText(context, "No se puede reintentar sin ID de video.", Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Text("Reintentar")
                    }
                }
            }
            is ResultsUiState.Idle -> {
                // Este estado debería ser breve o manejado por el chequeo de videoId null
                Text("Iniciando carga de resultados...")
                Spacer(modifier = Modifier.height(64.dp))
                CircularProgressIndicator()
            }
        }
    }
}

// Los Composables ResultCard, AnxietyLevelCard, DisplayEmotionBar, generateColorFromString y Preview
// permanecen como te los di en el mensaje anterior, ya que su lógica interna no dependía
// de `paddingValues` pasados desde ResultsScreen.

// (Pega aquí el código de ResultCard, AnxietyLevelCard, DisplayEmotionBar, generateColorFromString y PreviewResultsScreenContent
//  de los mensajes anteriores, asegurándote que NO tomen `paddingValues` si no es para su propio layout interno.)
@Composable
fun ResultCard(emotionPercentages: Map<String, Float>) {
    val colors = remember(emotionPercentages.keys) {
        emotionPercentages.keys.associateWith { generateColorFromString(it) }
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text("Distribución Emocional", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 12.dp))
            emotionPercentages.entries.toList().sortedByDescending { it.value }.forEach { (emotion, percentage) ->
                DisplayEmotionBar(emotion = emotion, percentage = percentage, color = colors[emotion] ?: Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun AnxietyLevelCard(level: Float) {
    val backgroundColor = MaterialTheme.colorScheme.surface
    val primaryColor = MaterialTheme.colorScheme.primary

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Nivel de Ansiedad Estimado",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier.height(130.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(
                    modifier = Modifier
                        .width(180.dp)
                        .height(90.dp)
                ) {
                    val strokeWidth = 18.dp.toPx()
                    val clampedLevel = level.coerceIn(0f, 100f)
                    val sweepAngle = (180f * (clampedLevel / 100f))

                    drawArc(
                        color = backgroundColor,
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                        size = Size(size.width, size.height * 2)
                    )
                    drawArc(
                        color = primaryColor,
                        startAngle = 180f,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                        size = Size(size.width, size.height * 2)
                    )
                    val angle = Math.toRadians((180f + sweepAngle).toDouble())
                    val radius = size.width / 2f
                    val center = Offset(size.width / 2f, size.height)
                    val x = center.x + (radius * cos(angle)).toFloat()
                    val y = center.y + (radius * sin(angle)).toFloat()
                    drawCircle(color = Color.White, radius = strokeWidth / 2 * 1.2f, center = Offset(x, y))
                    drawCircle(
                        color = primaryColor,
                        radius = strokeWidth / 2 * 0.8f,
                        center = Offset(x, y)
                    )
                }
                Text(
                    text = "${"%.1f".format(level)}%",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.align(Alignment.BottomCenter).offset(y = (-10).dp)
                )
            }
        }
    }
}

@Composable
fun DisplayEmotionBar(emotion: String, percentage: Float, color: Color) {
    val progress = remember(percentage) { percentage / 100f }
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = emotion, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
            Text(text = "${"%.1f".format(percentage)}%", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = color.copy(alpha = 0.25f)
        )
    }
}
 
fun generateColorFromString(input: String): Color {
    val seed = input.hashCode()
    val random = Random(seed.toLong())
    return Color(red = random.nextInt(256), green = random.nextInt(256), blue = random.nextInt(256))
}

@Preview(showBackground = true)
@Composable
fun PreviewResultsScreenContent() {
    val sampleEmotions = mapOf(
        "Feliz" to 65.2f, "Neutral" to 15.1f, "Sorpresa" to 10.5f, "Triste" to 5.0f,
        "Enojado" to 2.1f, "Miedo" to 1.1f, "Disgustado" to 1.0f
    )
    val sampleAnxiety = 23.5f
    SisvitaG2Theme(darkTheme = false) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally ) {
            Text("Resultados del Análisis", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(bottom = 16.dp))
            ResultCard(emotionPercentages = sampleEmotions)
            Spacer(modifier = Modifier.height(16.dp))
            AnxietyLevelCard(level = sampleAnxiety)
            Spacer(modifier = Modifier.height(24.dp))
            EmotionRadarChart(emotionPercentages = sampleEmotions)  
            Spacer(modifier = Modifier.height(16.dp))  
            EmotionalTrendChart(emotionPercentages = sampleEmotions)

            Text("Recomendaciones", style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { }) {
                Text(text = "Escuchar las recomendaciones de IA")
            }
        }
    }
}


@Composable
fun EmotionRadarChart(emotionPercentages: Map<String, Float>) {
    // Definir emotions al inicio del composable para que esté disponible en todo el scope
    val emotions = emotionPercentages.keys.toList()
    val colors = remember(emotionPercentages.keys) {
        emotionPercentages.keys.associateWith { generateColorFromString(it) }
    }
    val primaryColor = MaterialTheme.colorScheme.primary
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Perfil Emocional Radar",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Canvas(
                modifier = Modifier
                    .size(200.dp)
                    .padding(16.dp)
            ) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val radius = size.minDimension / 2f * 0.8f
                // Remover esta línea: val emotions = emotionPercentages.keys.toList()
                val angleStep = 360f / emotions.size

                // Dibujar círculos concéntricos (guías)  
                for (i in 1..5) {  
                    val circleRadius = radius * (i / 5f)  
                    drawCircle(  
                        color = Color.Gray.copy(alpha = 0.3f),  
                        radius = circleRadius,  
                        center = center,  
                        style = Stroke(width = 1.dp.toPx())  
                    )  
                }  
                  
                // Dibujar ejes  
                emotions.forEachIndexed { index, emotion ->  
                    val angle = Math.toRadians((index * angleStep - 90).toDouble())  
                    val endX = center.x + (radius * cos(angle)).toFloat()  
                    val endY = center.y + (radius * sin(angle)).toFloat()  
                      
                    drawLine(  
                        color = Color.Gray.copy(alpha = 0.5f),  
                        start = center,  
                        end = Offset(endX, endY),  
                        strokeWidth = 1.dp.toPx()  
                    )  
                }  
                  
                // Dibujar polígono de datos  
                val dataPoints = mutableListOf<Offset>()  
                emotions.forEachIndexed { index, emotion ->  
                    val percentage = emotionPercentages[emotion] ?: 0f  
                    val normalizedValue = percentage / 100f  
                    val angle = Math.toRadians((index * angleStep - 90).toDouble())  
                    val pointRadius = radius * normalizedValue  
                    val x = center.x + (pointRadius * cos(angle)).toFloat()  
                    val y = center.y + (pointRadius * sin(angle)).toFloat()  
                    dataPoints.add(Offset(x, y))  
                }  
                  
                // Conectar puntos  
                for (i in dataPoints.indices) {  
                    val nextIndex = (i + 1) % dataPoints.size  
                    drawLine(
                        color = primaryColor,
                        start = dataPoints[i],
                        end = dataPoints[nextIndex],  
                        strokeWidth = 2.dp.toPx()  
                    )  
                }

                // Dibujar puntos de datos  
                dataPoints.forEachIndexed { index, point ->  
                    val emotion = emotions[index]  
                    drawCircle(  
                        color = colors[emotion] ?: Color.Gray,  
                        radius = 4.dp.toPx(),  
                        center = point  
                    )  
                }  
            }  
              
            // Leyenda  
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                items(emotions) { emotion ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    colors[emotion] ?: Color.Gray,
                                    CircleShape
                                )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = emotion,
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable  
fun EmotionalTrendChart(emotionPercentages: Map<String, Float>) {  
    // Categorizar emociones  
    val positiveEmotions = listOf("Feliz")  
    val negativeEmotions = listOf("Triste", "Enojado", "Miedo", "Disgustado")  
    val neutralEmotions = listOf("Neutral", "Sorpresa")  
      
    val positiveSum = positiveEmotions.sumOf { (emotionPercentages[it] ?: 0f).toDouble() }.toFloat()  
    val negativeSum = negativeEmotions.sumOf { (emotionPercentages[it] ?: 0f).toDouble() }.toFloat()  
    val neutralSum = neutralEmotions.sumOf { (emotionPercentages[it] ?: 0f).toDouble() }.toFloat()  
      
    val categories = mapOf(  
        "Positivas" to positiveSum,  
        "Negativas" to negativeSum,  
        "Neutras" to neutralSum  
    )  
      
    val categoryColors = mapOf(  
        "Positivas" to Color(0xFF4CAF50), // Verde  
        "Negativas" to Color(0xFFF44336), // Rojo  
        "Neutras" to Color(0xFF9E9E9E)    // Gris  
    )  
      
    Card(  
        modifier = Modifier.fillMaxWidth(),  
        shape = RoundedCornerShape(12.dp),  
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),  
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)  
    ) {  
        Column(  
            modifier = Modifier  
                .fillMaxWidth()  
                .padding(16.dp)  
        ) {  
            Text(  
                "Tendencia Emocional",  
                style = MaterialTheme.typography.titleMedium,  
                modifier = Modifier.padding(bottom = 12.dp)  
            )  
              
            // Gráfico de barras horizontales  
            categories.entries.sortedByDescending { it.value }.forEach { (category, percentage) ->  
                Column(modifier = Modifier.padding(vertical = 4.dp)) {  
                    Row(  
                        modifier = Modifier.fillMaxWidth(),  
                        horizontalArrangement = Arrangement.SpaceBetween,  
                        verticalAlignment = Alignment.CenterVertically  
                    ) {  
                        Row(verticalAlignment = Alignment.CenterVertically) {  
                            Box(  
                                modifier = Modifier  
                                    .size(12.dp)  
                                    .background(  
                                        categoryColors[category] ?: Color.Gray,  
                                        CircleShape  
                                    )  
                            )  
                            Spacer(modifier = Modifier.width(8.dp))  
                            Text(  
                                text = category,  
                                style = MaterialTheme.typography.bodyMedium,  
                                color = MaterialTheme.colorScheme.onSurfaceVariant  
                            )  
                        }  
                        Text(  
                            text = "${"%.1f".format(percentage)}%",  
                            style = MaterialTheme.typography.bodyMedium,  
                            color = MaterialTheme.colorScheme.onSurfaceVariant  
                        )  
                    }  
                      
                    Spacer(modifier = Modifier.height(4.dp))  
                      
                    LinearProgressIndicator(  
                        progress = { (percentage / 100f).coerceIn(0f, 1f) },  
                        modifier = Modifier  
                            .fillMaxWidth()  
                            .height(8.dp)  
                            .clip(RoundedCornerShape(4.dp)),  
                        color = categoryColors[category] ?: Color.Gray,  
                        trackColor = (categoryColors[category] ?: Color.Gray).copy(alpha = 0.25f)  
                    )  
                }  
                Spacer(modifier = Modifier.height(8.dp))  
            }  
              
            // Gráfico circular (donut)  
            Spacer(modifier = Modifier.height(16.dp))  
            Box(  
                modifier = Modifier  
                    .size(120.dp)  
                    .align(Alignment.CenterHorizontally)  
            ) {  
                Canvas(modifier = Modifier.fillMaxSize()) {  
                    val total = categories.values.sum()  
                    if (total > 0) {  
                        var startAngle = -90f  
                        categories.forEach { (category, value) ->  
                            val sweepAngle = (value / total) * 360f  
                            drawArc(  
                                color = categoryColors[category] ?: Color.Gray,  
                                startAngle = startAngle,  
                                sweepAngle = sweepAngle,  
                                useCenter = false,  
                                style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)  
                            )  
                            startAngle += sweepAngle  
                        }  
                    }  
                }  
                  
                // Texto central  
                Column(  
                    modifier = Modifier.align(Alignment.Center),  
                    horizontalAlignment = Alignment.CenterHorizontally  
                ) {  
                    Text(  
                        text = "Total",  
                        style = MaterialTheme.typography.bodySmall,  
                        color = MaterialTheme.colorScheme.onSurfaceVariant  
                    )  
                    Text(  
                        text = "${"%.1f".format(categories.values.sum())}%",  
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),  
                        color = MaterialTheme.colorScheme.primary  
                    )  
                }  
            }  
        }  
    }  
}