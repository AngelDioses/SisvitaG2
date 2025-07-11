package com.example.sisvitag2.ui.screens.camera

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.core.ImageCapture
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.sisvitag2.R
import com.example.sisvitag2.ui.screens.loading.LoadingViewModel
import com.example.sisvitag2.data.repository.emotionalAnalysis.EmotionalAnalysisResponse
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.androidx.compose.get
import kotlinx.coroutines.delay
import androidx.compose.foundation.Canvas
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.tween
import androidx.compose.animation.with
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@Composable
fun CameraScreen(navController: NavController) {
    val context = LocalContext.current
    // Obtiene ViewModels
    val cameraViewModel: CameraScreenViewModel = koinViewModel() // Cambiado a koinViewModel
    val loadingViewModel: LoadingViewModel = koinViewModel() // Obtiene de Koin
    
    // Obtener el repositorio usando get()
    val analysisRepository = get<com.example.sisvitag2.data.repository.emotionalAnalysis.EmotionalAnalysisRepository>()

    // Inyectar el repositorio en el ViewModel
    LaunchedEffect(analysisRepository) {
        cameraViewModel.setAnalysisRepository(analysisRepository)
    }

    var hasCameraPermission by remember { mutableStateOf(false) }
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (!isGranted) {
            Toast.makeText(context, "Permiso de cámara denegado.", Toast.LENGTH_SHORT).show()
        }
    }

    // Comprobar y solicitar permiso al entrar
    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            hasCameraPermission = true
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    if (hasCameraPermission /* && recordAudioGranted si es necesario */) {
        CameraContent(
            navController = navController,
            cameraViewModel = cameraViewModel,
            loadingViewModel = loadingViewModel
        )
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "Se requieren permisos de cámara...", color = Color.Gray)
        }
    }
}

@Composable
fun CameraContent(
    navController: NavController,
    cameraViewModel: CameraScreenViewModel,
    loadingViewModel: LoadingViewModel
) {
    // Observa estados del CameraViewModel
    val isFlashOn by cameraViewModel.isFlashOn.collectAsState()
    val isRecording by cameraViewModel.isRecording.collectAsState()
    val isUsingFrontCamera by cameraViewModel.isUsingFrontCamera.collectAsState()
    val isAnalyzing by cameraViewModel.isAnalyzing.collectAsState()
    val analysisResult by cameraViewModel.analysisResult.collectAsState()
    val analysisError by cameraViewModel.analysisError.collectAsState()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Configuración de CameraX
    val recorder = remember { Recorder.Builder().setQualitySelector(QualitySelector.from(Quality.SD)).build() } // Usar SD puede ser suficiente
    val videoCapture: VideoCapture<Recorder> = remember { VideoCapture.withOutput(recorder) }
    val imageCapture = remember { ImageCapture.Builder().build() }

    Box(modifier = Modifier.fillMaxSize()) {
        // Camera preview
        CameraPreview(
            isUsingFrontCamera = isUsingFrontCamera,
            videoCapture = videoCapture,
            imageCapture = imageCapture,
            viewModel = cameraViewModel
        )

        // Mostrar resultados del análisis si están disponibles
        analysisResult?.let { result ->
            AnalysisResultsOverlay(
                result = result,
                onClose = { cameraViewModel.clearAnalysisResults() }
            )
        }

        // Mostrar error si ocurrió
        analysisError?.let { error ->
            ErrorOverlay(
                error = error,
                onClose = { cameraViewModel.clearAnalysisResults() }
            )
        }

        // Botones en la parte inferior
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 64.dp), // Aplicar paddings separados
            // ---------------------------
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Flash Button
            IconButton(
                onClick = { cameraViewModel.toggleFlash() },
                modifier = Modifier.size(56.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary)
            ) {
                Icon(painter = painterResource(id = if (isFlashOn) R.drawable.ic_flash_on else R.drawable.ic_flash_off), contentDescription = "Flash", tint = Color.White )
            }

            // Photo Button
            IconButton(
                onClick = {
                    Log.d("CameraContent", "Botón de foto presionado.")
                    cameraViewModel.takePhoto(context, imageCapture)
                },
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondary),
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_camera),
                    contentDescription = "Take Photo",
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }

            // Record Button
            IconButton(
                onClick = {
                    if (isRecording) {
                        Log.d("CameraContent", "Botón Detener presionado.")
                        cameraViewModel.stopRecording() // Llama a stop (espera callback Finalize)
                        // El estado isRecording cambiará a false en el callback del ViewModel

                        // Intentamos obtener la URI (puede ser null si stop se llamó antes de Finalize)
                        // Es MÁS SEGURO reaccionar a un StateFlow<Uri?> en el ViewModel,
                        // pero para simplificar, intentamos obtenerla aquí después de llamar a stop.
                        scope.launch {
                            delay(500) // Pequeña espera para dar tiempo al callback Finalize (NO IDEAL)
                            // --- CORRECCIÓN: Llama a la función implementada ---
                            val finalVideoUri: Uri? = cameraViewModel.getLastRecordedVideoUri()
                            // ---------------------------------------------------
                            if (finalVideoUri != null) {
                                Log.i("CameraContent", "Video grabado obtenido: $finalVideoUri")
                                // NUEVO: Iniciar análisis directo
                                cameraViewModel.analyzeRecordedVideo()
                            } else {
                                Log.e("CameraContent", "No se pudo obtener la Uri del video grabado después de detener.")
                                Toast.makeText(context, "Error al guardar el video.", Toast.LENGTH_SHORT).show()
                                // Asegurarse que el estado de grabación se resetee en el ViewModel incluso si hay error
                            }
                        }

                    } else {
                        // --- AL INICIAR ---
                        Log.d("CameraContent", "Botón Iniciar presionado.")
                        // Simplemente inicia la grabación. El estado isRecording se actualiza en el ViewModel.
                        cameraViewModel.startRecording(context, videoCapture)
                    }
                },
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(if (isRecording) Color.Red else MaterialTheme.colorScheme.primary),
                // Deshabilitar brevemente después de presionar para evitar doble tap?
                // enabled = !isProcessingAction // Añadir estado local si es necesario
            ) {
                Icon( /* ... (icono record/stop sin cambios) ... */
                    painter = painterResource(id = if (isRecording) R.drawable.ic_stop_rec else R.drawable.ic_play_rec),
                    contentDescription = "Record",
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }

            // Switch Camera Button
            IconButton(
                onClick = { cameraViewModel.toggleCamera() },
                modifier = Modifier.size(56.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary)
            ) {
                Icon(painter = painterResource(id = R.drawable.ic_flip_camera), contentDescription = "Switch Camera", tint = Color.White)
            }
        }

        // Indicador de análisis
        if (isAnalyzing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Analizando emociones...",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AnalysisResultsOverlay(
    result: EmotionalAnalysisResponse,
    onClose: () -> Unit
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Gráfico de Barras", "Gráfico de Pastel", "Recomendaciones")
    
    // Obtener el repositorio de Gemini
    val geminiRepository = get<com.example.sisvitag2.data.repository.gemini.GeminiRepository>()
    
    // Estado para la recomendación
    var recommendation by remember { mutableStateOf<String?>(null) }
    var isLoadingRecommendation by remember { mutableStateOf(false) }
    var recommendationError by remember { mutableStateOf<String?>(null) }
    
    // Generar recomendación cuando se selecciona la pestaña de recomendaciones
    LaunchedEffect(selectedTabIndex) {
        if (selectedTabIndex == 2 && recommendation == null && !isLoadingRecommendation) {
            isLoadingRecommendation = true
            recommendationError = null
            
            geminiRepository.generateRecommendations(result)
                .collect { result ->
                    isLoadingRecommendation = false
                    if (result.isSuccess) {
                        recommendation = result.getOrNull()
                    } else {
                        recommendationError = result.exceptionOrNull()?.message ?: "Error desconocido"
                    }
                }
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp)
                .padding(bottom = 120.dp), // Agregar padding extra abajo para evitar superposición
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Resultados del Análisis",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Emoción dominante: ${translateEmotionToSpanish(result.getDominantEmotion())}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Pestañas
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { Text(title) }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Contenido de las pestañas
                AnimatedContent(
                    targetState = selectedTabIndex,
                    transitionSpec = {
                        slideInHorizontally(
                            initialOffsetX = { if (targetState > initialState) it else -it },
                            animationSpec = tween(300)
                        ) + fadeIn(animationSpec = tween(300)) with
                        slideOutHorizontally(
                            targetOffsetX = { if (targetState > initialState) -it else it },
                            animationSpec = tween(300)
                        ) + fadeOut(animationSpec = tween(300))
                    },
                    label = "TabContent"
                ) { tabIndex ->
                    when (tabIndex) {
                        0 -> BarChartContent(result = result)
                        1 -> PieChartContent(result = result)
                        2 -> RecommendationContent(
                            recommendation = recommendation,
                            isLoading = isLoadingRecommendation,
                            error = recommendationError
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = onClose,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cerrar")
                }
            }
        }
    }
}

@Composable
fun EmotionBar(
    emotionName: String,
    value: Int,
    total: Int
) {
    val percentage = if (total > 0) (value.toFloat() / total * 100) else 0f
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = emotionName,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "(${String.format("%.1f", percentage)}%)",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        LinearProgressIndicator(
            progress = percentage / 100f,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
fun BarChartContent(result: EmotionalAnalysisResponse) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .height(320.dp)
    ) {
        item {
            Text(
                text = "Distribución de Emociones",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
        
        item {
            EmotionBar("Feliz", result.happy, result.getTotalEmotions())
        }
        item {
            EmotionBar("Triste", result.sad, result.getTotalEmotions())
        }
        item {
            EmotionBar("Enojado", result.angry, result.getTotalEmotions())
        }
        item {
            EmotionBar("Miedo", result.fear, result.getTotalEmotions())
        }
        item {
            EmotionBar("Sorpresa", result.surprise, result.getTotalEmotions())
        }
        item {
            EmotionBar("Disgusto", result.disgust, result.getTotalEmotions())
        }
        item {
            EmotionBar("Neutral", result.neutral, result.getTotalEmotions())
        }
    }
}

@Composable
fun PieChartContent(result: EmotionalAnalysisResponse) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .height(320.dp)
    ) {
        item {
            Text(
                text = "Distribución de Emociones",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
        
        item {
            PieChartCompose(result = result)
        }
    }
}

@Composable
fun RecommendationContent(
    recommendation: String?,
    isLoading: Boolean,
    error: String?
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .height(320.dp)
    ) {
        item {
            Text(
                text = "Recomendaciones Personalizadas",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
        
        item {
            when {
                isLoading -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(48.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Generando recomendaciones...",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                error != null -> {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Error al generar recomendaciones",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = error,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
                
                recommendation != null -> {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = recommendation,
                                fontSize = 14.sp,
                                lineHeight = 20.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                else -> {
                    Text(
                        text = "Selecciona esta pestaña para generar recomendaciones personalizadas",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun PieChartCompose(result: EmotionalAnalysisResponse) {
    val total = result.getTotalEmotions()
    val emotions = listOf(
        Triple("Feliz", result.happy, Color(0xFF4CAF50)),
        Triple("Triste", result.sad, Color(0xFF2196F3)),
        Triple("Enojado", result.angry, Color(0xFFF44336)),
        Triple("Miedo", result.fear, Color(0xFF9C27B0)),
        Triple("Sorpresa", result.surprise, Color(0xFFFF9800)),
        Triple("Disgusto", result.disgust, Color(0xFF795548)),
        Triple("Neutral", result.neutral, Color(0xFF607D8B))
    )
    val filtered = emotions.filter { it.second > 0 }
    if (total == 0 || filtered.isEmpty()) {
        Text(
            text = "No hay datos de emociones disponibles",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(16.dp)
        )
        return
    }
    val proportions = filtered.map { it.second.toFloat() / total }
    val colors = filtered.map { it.third }
    val labels = filtered.map { it.first }
    val values = filtered.map { it.second }
    val sweepAngles = proportions.map { it * 360f }
    
    Column(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Gráfico de pastel centrado
        Canvas(
            modifier = Modifier
                .size(140.dp)
                .padding(16.dp)
        ) {
            var startAngle = -90f
            for (i in sweepAngles.indices) {
                drawArc(
                    color = colors[i],
                    startAngle = startAngle,
                    sweepAngle = sweepAngles[i],
                    useCenter = true
                )
                startAngle += sweepAngles[i]
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Leyenda debajo del gráfico
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            for (i in filtered.indices) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(colors[i], CircleShape)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = "${labels[i]} (${String.format("%.1f", proportions[i]*100)}%)",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun EmotionLegendItem(
    emotionName: String,
    value: Int,
    total: Int,
    color: Color
) {
    val percentage = if (total > 0) (value.toFloat() / total * 100) else 0f
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, CircleShape)
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
                            Text(
                        text = "$emotionName (${String.format("%.1f", percentage)}%)",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
    }
}

@Composable
fun ErrorOverlay(
    error: String,
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Error en el Análisis",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = error,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = onClose,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cerrar")
                }
            }
        }
    }
}

// CameraPreview Composable (Sin cambios funcionales necesarios aquí)
@Composable
fun CameraPreview(
    isUsingFrontCamera: Boolean,
    videoCapture: VideoCapture<Recorder>,
    imageCapture: ImageCapture,
    viewModel: CameraScreenViewModel
) {
    // ... (Código existente de CameraPreview) ...
    val context = LocalContext.current
    val lifecycleOwner = LocalContext.current as LifecycleOwner
    val previewView = remember { PreviewView(context) }
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    LaunchedEffect(cameraProviderFuture, isUsingFrontCamera) { // Key incluye isUsingFrontCamera
        val provider = cameraProviderFuture.get()
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val cameraSelector = if (isUsingFrontCamera) CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }
            try {
                provider.unbindAll()
                val camera = provider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, videoCapture, imageCapture)
                viewModel.setCamera(camera)
            } catch (e: Exception) {
                Log.e("CameraPreview", "Error vinculando ciclo de vida", e)
            }
        }, ContextCompat.getMainExecutor(context))
    }
    AndroidView(modifier = Modifier.fillMaxSize(), factory = { previewView })
}

private fun translateEmotionToSpanish(emotion: String): String {
    return when (emotion.lowercase()) {
        "happy" -> "Feliz"
        "sad" -> "Triste"
        "angry" -> "Enojado"
        "fear" -> "Miedo"
        "surprise" -> "Sorpresa"
        "disgust" -> "Disgusto"
        "neutral" -> "Neutral"
        else -> emotion.capitalize()
    }
}