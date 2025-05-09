package com.example.sisvitag2.ui.screens.results

import android.net.Uri // No parece usarse aquí, considera eliminar si no es necesario
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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

                if (emotions != null) {
                    val emotionPercentages = mapFirebaseEmotionsToFloatPercentages(emotions)

                    ResultCard(emotionPercentages = emotionPercentages)
                    Spacer(modifier = Modifier.height(16.dp))
                    AnxietyLevelCard(level = anxietyLevel)
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
                            // userName se obtiene del ViewModel (SessionViewModel) en OrientationScreen
                            // Ya no es necesario pasarlo explícitamente en la ruta si OrientationScreen lo toma de allí.
                            try {
                                val emotionsJson = Json.encodeToString(emotions)
                                val encodedEmotions = URLEncoder.encode(emotionsJson, StandardCharsets.UTF_8.toString())
                                // Navega a OrientationScreen pasando solo las emociones
                                navController.navigate("Orientations?emotions=$encodedEmotions")
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error al preparar datos para orientación.", Toast.LENGTH_SHORT).show()
                                Log.e("ResultsScreen", "Error serializando/codificando emociones", e)
                            }
                        }
                        // enabled se podría basar en si hay emociones, el nombre ya no es un factor aquí
                    ) {
                        Text(text = "Escuchar las recomendaciones de IA")
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
            Text("Recomendaciones", style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { }) {
                Text(text = "Escuchar las recomendaciones de IA")
            }
        }
    }
}