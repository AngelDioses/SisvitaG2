package com.example.sisvitag2.ui.screens.orientation

import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination // Para popUpTo
import androidx.navigation.compose.rememberNavController
import com.airbnb.lottie.LottieComposition
import com.airbnb.lottie.compose.*
import com.example.sisvitag2.R
import com.example.sisvitag2.data.repository.emotionalAnalysis.EmotionalAnalysisResponse
import com.example.sisvitag2.ui.theme.SisvitaG2Theme
import com.linc.audiowaveform.AudioWaveform
import com.linc.audiowaveform.model.WaveformAlignment
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.util.Locale
import java.util.UUID
import kotlin.random.Random

@Composable
fun OrientationScreen(
    // paddingValues: PaddingValues, // ELIMINADO
    rootNavController: NavController, // Este es el NavController principal de AppNavHost
    userName: String?,
    emotionsData: EmotionalAnalysisResponse?,
    viewModel: OrientationViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    var isTtsInitialized by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(false) }
    var currentMessages by remember { mutableStateOf<List<String>>(emptyList()) }
    var amplitudes by remember { mutableStateOf(List(30) { 0 }) }
    val scope = rememberCoroutineScope()
    var animationJob by remember { mutableStateOf<Job?>(null) }

    val lottieComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.roboto))
    val progress by animateLottieCompositionAsState(
        composition = lottieComposition, iterations = LottieConstants.IterateForever,
        speed = if (isPlaying) 1f else 0.5f,
        isPlaying = true
    )

    LaunchedEffect(Unit) {
        Log.d("OrientationScreen", "Inicializando TTS...")
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale("es", "ES")
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        scope.launch {
                            isPlaying = true
                            animationJob?.cancel()
                            animationJob = launch {
                                while (isActive) {
                                    amplitudes = List(30) { (Random.nextFloat() * 80f + 20f).toInt() }
                                    delay(100)
                                }
                            }
                        }
                    }
                    override fun onDone(utteranceId: String?) {
                        scope.launch {
                            if (tts?.isSpeaking == false) {
                                isPlaying = false
                                animationJob?.cancel()
                                amplitudes = List(30) { 0 }
                            }
                        }
                    }
                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        scope.launch {
                            isPlaying = false; animationJob?.cancel(); amplitudes = List(30) { 0 }
                            Toast.makeText(context, "Error de TTS", Toast.LENGTH_SHORT).show()
                        }
                    }
                })
                isTtsInitialized = true
                Log.d("OrientationScreen", "TTS inicializado correctamente.")
            } else {
                Log.e("OrientationScreen", "Fallo al inicializar TTS, status: $status")
                Toast.makeText(context, "No se pudo inicializar servicio de voz.", Toast.LENGTH_LONG).show()
            }
        }
    }

    LaunchedEffect(userName, emotionsData, viewModel, isTtsInitialized) {
        if (isTtsInitialized && uiState is OrientationUiState.Idle && userName != null && emotionsData != null) {
            Log.d("OrientationScreen", "TTS listo y datos disponibles. Llamando a fetchOrientation.")
            viewModel.fetchOrientation(userName, emotionsData)
        } else if (uiState is OrientationUiState.Idle && (userName == null || emotionsData == null)) {
            Log.w("OrientationScreen", "userName o emotionsData son nulos en estado Idle.")
        }
    }

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is OrientationUiState.Success -> {
                currentMessages = state.response.response
                Log.d("OrientationScreen", "Mensajes de orientación recibidos: ${currentMessages.size}")
            }
            is OrientationUiState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                Log.e("OrientationScreen", "Error en UiState: ${state.message}")
            }
            else -> { /* No-op */ }
        }
    }

    OrientationContent(
        navController = rootNavController, // Pasa el rootNavController a OrientationContent
        amplitudes = amplitudes,
        lottieComposition = lottieComposition,
        progress = progress,
        isPlaying = isPlaying,
        isLoading = uiState is OrientationUiState.Loading,
        onPlayPauseClick = {
            if (!isTtsInitialized) {
                Toast.makeText(context, "Servicio de voz aún no está listo.", Toast.LENGTH_SHORT).show(); return@OrientationContent
            }
            if (isPlaying) {
                tts?.stop()
            } else {
                if (currentMessages.isNotEmpty()) {
                    playMessages(tts, currentMessages)
                } else {
                    Toast.makeText(context, "No hay mensajes de orientación para reproducir.", Toast.LENGTH_SHORT).show()
                    if (uiState is OrientationUiState.Success && userName != null && emotionsData != null) {
                        viewModel.fetchOrientation(userName, emotionsData)
                    }
                }
            }
        }
    )

    DisposableEffect(Unit) {
        onDispose {
            Log.d("OrientationScreen", "Limpiando TTS y Job de animación.")
            animationJob?.cancel()
            tts?.stop()
            tts?.shutdown()
            tts = null // Ayuda al GC
        }
    }
}

private fun playMessages(tts: TextToSpeech?, messages: List<String>) {
    tts?.let { textToSpeech ->
        textToSpeech.stop()
        messages.forEachIndexed { index, message ->
            val utteranceId = "utterance_${UUID.randomUUID()}"
            textToSpeech.speak(message, TextToSpeech.QUEUE_ADD, null, utteranceId)
        }
    }
}

@Composable
fun OrientationContent(
    navController: NavController, // Este es el rootNavController
    amplitudes: List<Int>,
    lottieComposition: LottieComposition?,
    progress: Float,
    isPlaying: Boolean,
    isLoading: Boolean,
    onPlayPauseClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp), // Padding interno propio de la pantalla
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (lottieComposition != null) {
                LottieAnimation(
                    composition = lottieComposition,
                    progress = { progress },
                    modifier = Modifier.size(200.dp)
                )
            } else {
                Spacer(modifier = Modifier.height(200.dp))
            }

            AudioWaveform(
                modifier = Modifier.fillMaxWidth().height(100.dp),
                amplitudes = amplitudes,
                spikeWidth = 4.dp, spikePadding = 2.dp, spikeRadius = 4.dp,
                progress = if (isPlaying) 1f else 0f,
                progressBrush = SolidColor(MaterialTheme.colorScheme.primary),
                waveformBrush = SolidColor(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)),
                waveformAlignment = WaveformAlignment.Bottom,
                onProgressChange = { /* Lambda vacío requerido */ } // <--- CORREGIDO
            )
            Spacer(modifier = Modifier.height(24.dp))

            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(60.dp)) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(60.dp))
                } else {
                    IconButton(
                        onClick = onPlayPauseClick,
                        colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.fillMaxSize().clip(CircleShape)
                    ) {
                        Icon(
                            painter = painterResource(id = if (isPlaying) R.drawable.ic_stop_ia else R.drawable.ic_play_ia),
                            contentDescription = if (isPlaying) "Pausar" else "Reproducir",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = when {
                    isLoading -> "Obteniendo orientación..."
                    isPlaying -> "Reproduciendo..."
                    else -> "Presiona play para escuchar"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }

        Button(
            onClick = {
                // 'navController' aquí ES el 'rootNavController'
                navController.navigate("Inicio") {
                    // Pop hasta el inicio del grafo del rootNavController (AppNavHost)
                    popUpTo(navController.graph.findStartDestination().id) { // <--- CORREGIDO
                        inclusive = false // No quita "Inicio" de la pila de AppNavHost
                    }
                    launchSingleTop = true // Evita múltiples instancias de "Inicio"
                }
            },
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
        ) {
            Text("Ir al inicio")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OrientationContentPreview() {
    SisvitaG2Theme(darkTheme = false) {
        val navController = rememberNavController()
        val amplitudes = remember { List(30) { Random.nextInt(20, 80) } }
        OrientationContent(
            navController = navController,
            amplitudes = amplitudes,
            lottieComposition = null,
            progress = 0f,
            isPlaying = false,
            isLoading = false,
            onPlayPauseClick = {}
        )
    }
}