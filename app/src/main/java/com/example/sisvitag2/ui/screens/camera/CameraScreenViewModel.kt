package com.example.sisvitag2.ui.screens.camera

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.camera.core.Camera
import androidx.camera.core.TorchState
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.camera.video.VideoCapture
import androidx.camera.video.Recorder
import androidx.camera.video.FileOutputOptions
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import java.io.File
import android.util.Log
import androidx.camera.video.Recording // Mantener el import para la clase Recording
import androidx.camera.video.VideoRecordEvent
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.example.sisvitag2.data.repository.emotionalAnalysis.EmotionalAnalysisRepository
import com.example.sisvitag2.data.repository.emotionalAnalysis.EmotionalAnalysisResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CameraScreenViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "CameraScreenViewModel"
    }

    private val app = application
    private var camera: Camera? = null
    private var activeRecording: Recording? = null
    private val _lastRecordedVideoUri = MutableStateFlow<Uri?>(null)
    private val _lastCapturedImageUri = MutableStateFlow<Uri?>(null)
    private val _isFlashOn = MutableStateFlow(false)
    val isFlashOn: StateFlow<Boolean> = _isFlashOn.asStateFlow()
    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean>  = _isRecording.asStateFlow()
    private val _isUsingFrontCamera = MutableStateFlow(false)
    val isUsingFrontCamera: StateFlow<Boolean> = _isUsingFrontCamera.asStateFlow()
    
    // Nuevos estados para el análisis directo
    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()
    
    private val _analysisResult = MutableStateFlow<EmotionalAnalysisResponse?>(null)
    val analysisResult: StateFlow<EmotionalAnalysisResponse?> = _analysisResult.asStateFlow()
    
    private val _analysisError = MutableStateFlow<String?>(null)
    val analysisError: StateFlow<String?> = _analysisError.asStateFlow()

    // Repositorio para análisis emocional (se inyectará desde Koin)
    private var analysisRepository: EmotionalAnalysisRepository? = null
    
    fun setAnalysisRepository(repository: EmotionalAnalysisRepository) {
        this.analysisRepository = repository
    }

    fun toggleFlash() {
        camera?.let { cam ->
            if (cam.cameraInfo.hasFlashUnit()) {
                val newState = !_isFlashOn.value
                Log.d(TAG, "Cambiando linterna a: $newState")
                cam.cameraControl.enableTorch(newState)
                    .addListener({ _isFlashOn.value = newState }, ContextCompat.getMainExecutor(app))
            } else {
                Log.w(TAG, "Cámara sin flash.")
            }
        }
    }

    fun setCamera(camera: Camera) {
        this.camera = camera
        _isFlashOn.value = camera.cameraInfo.torchState.value == TorchState.ON
    }

    fun toggleCamera() {
        val currentFlashState = _isFlashOn.value
        _isUsingFrontCamera.value = !_isUsingFrontCamera.value
        if (currentFlashState) {
            camera?.cameraControl?.enableTorch(false)?.addListener({ _isFlashOn.value = false }, ContextCompat.getMainExecutor(app))
        }
        Log.d(TAG, "Cambiando a cámara: ${if (_isUsingFrontCamera.value) "Frontal" else "Trasera"}")
    }

    /**
     * Toma una foto usando ImageCapture
     */
    fun takePhoto(context: Context, imageCapture: ImageCapture) {
        Log.d(TAG, "Tomando foto...")
        
        val photoFile = File(context.cacheDir, "temp_photo_${System.currentTimeMillis()}.jpg")
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
        
        // Limpiar resultados previos
        clearAnalysisResults()
        
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val savedUri = outputFileResults.savedUri ?: photoFile.toUri()
                    Log.i(TAG, "Foto guardada exitosamente: $savedUri")
                    _lastCapturedImageUri.value = savedUri
                    
                    // Analizar la foto inmediatamente
                    analyzeCapturedImage()
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e(TAG, "Error al tomar foto", exception)
                    _analysisError.value = "Error al tomar foto: ${exception.message}"
                }
            }
        )
    }

    /**
     * Analiza la foto capturada usando la API externa
     */
    fun analyzeCapturedImage() {
        val imageUri = _lastCapturedImageUri.value
        if (imageUri == null) {
            Log.e(TAG, "No hay imagen capturada para analizar")
            _analysisError.value = "No hay imagen capturada para analizar"
            return
        }
        
        if (analysisRepository == null) {
            Log.e(TAG, "Repositorio de análisis no inicializado")
            _analysisError.value = "Error de configuración: repositorio no disponible"
            return
        }
        
        _isAnalyzing.value = true
        _analysisError.value = null
        _analysisResult.value = null
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d(TAG, "Iniciando análisis directo de la imagen: $imageUri")
                val result = analysisRepository!!.analyzeImageDirectly(imageUri)
                
                withContext(Dispatchers.Main) {
                    _analysisResult.value = result
                    _isAnalyzing.value = false
                    Log.i(TAG, "Análisis de imagen completado exitosamente: $result")
                    
                    // IMPRIMIR RESULTADOS EN CONSOLA
                    Log.i(TAG, "=== RESULTADOS DE IMAGEN RECIBIDOS EN CAMERA VIEWMODEL ===")
                    Log.i(TAG, "Angry: ${result.angry}")
                    Log.i(TAG, "Disgust: ${result.disgust}")
                    Log.i(TAG, "Fear: ${result.fear}")
                    Log.i(TAG, "Happy: ${result.happy}")
                    Log.i(TAG, "Sad: ${result.sad}")
                    Log.i(TAG, "Surprise: ${result.surprise}")
                    Log.i(TAG, "Neutral: ${result.neutral}")
                    Log.i(TAG, "Total: ${result.getTotalEmotions()}")
                    Log.i(TAG, "Emoción dominante: ${result.getDominantEmotion()}")
                    Log.i(TAG, "=== FIN RESULTADOS DE IMAGEN ===")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error durante el análisis de la imagen", e)
                withContext(Dispatchers.Main) {
                    _analysisError.value = "Error en el análisis de imagen: ${e.message}"
                    _isAnalyzing.value = false
                }
            }
        }
    }

    /**
     * NUEVO MÉTODO: Analiza el video grabado directamente usando la API externa
     */
    fun analyzeRecordedVideo() {
        val videoUri = _lastRecordedVideoUri.value
        if (videoUri == null) {
            Log.e(TAG, "No hay video grabado para analizar")
            _analysisError.value = "No hay video grabado para analizar"
            return
        }
        
        if (analysisRepository == null) {
            Log.e(TAG, "Repositorio de análisis no inicializado")
            _analysisError.value = "Error de configuración: repositorio no disponible"
            return
        }
        
        _isAnalyzing.value = true
        _analysisError.value = null
        _analysisResult.value = null
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d(TAG, "Iniciando análisis directo del video: $videoUri")
                val result = analysisRepository!!.analyzeVideoDirectly(videoUri)
                
                withContext(Dispatchers.Main) {
                    _analysisResult.value = result
                    _isAnalyzing.value = false
                    Log.i(TAG, "Análisis completado exitosamente: $result")
                    
                    // IMPRIMIR RESULTADOS EN CONSOLA
                    Log.i(TAG, "=== RESULTADOS RECIBIDOS EN CAMERA VIEWMODEL ===")
                    Log.i(TAG, "Angry: ${result.angry}")
                    Log.i(TAG, "Disgust: ${result.disgust}")
                    Log.i(TAG, "Fear: ${result.fear}")
                    Log.i(TAG, "Happy: ${result.happy}")
                    Log.i(TAG, "Sad: ${result.sad}")
                    Log.i(TAG, "Surprise: ${result.surprise}")
                    Log.i(TAG, "Neutral: ${result.neutral}")
                    Log.i(TAG, "Total: ${result.getTotalEmotions()}")
                    Log.i(TAG, "Emoción dominante: ${result.getDominantEmotion()}")
                    Log.i(TAG, "=== FIN RESULTADOS CAMERA VIEWMODEL ===")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error durante el análisis del video", e)
                withContext(Dispatchers.Main) {
                    _analysisError.value = "Error en el análisis: ${e.message}"
                    _isAnalyzing.value = false
                }
            }
        }
    }
    
    /**
     * Limpia los resultados del análisis
     */
    fun clearAnalysisResults() {
        _analysisResult.value = null
        _analysisError.value = null
        _isAnalyzing.value = false
    }

    @SuppressLint("MissingPermission")
    fun startRecording(context: Context, videoCapture: VideoCapture<Recorder>) {
        if (activeRecording != null || _isRecording.value) { Log.w(TAG, "Grabación ya en progreso."); return }
        val videoFile = File(context.cacheDir, "temp_video_${System.currentTimeMillis()}.mp4")
        Log.d(TAG, "Preparando grabación en: ${videoFile.absolutePath}")
        _lastRecordedVideoUri.value = null
        _isRecording.value = true
        
        // Limpiar resultados previos
        clearAnalysisResults()

        try {
            val outputOptions = FileOutputOptions.Builder(videoFile).build()
            activeRecording = videoCapture.output
                .prepareRecording(context, outputOptions)
                // .withAudioEnabled()
                .start(ContextCompat.getMainExecutor(context)) { recordEvent ->
                    when (recordEvent) {
                        is VideoRecordEvent.Start -> Log.i(TAG, "Evento: Grabación iniciada.")
                        is VideoRecordEvent.Finalize -> {
                            val error = recordEvent.error
                            val outputUri = recordEvent.outputResults.outputUri
                            Log.d(TAG, "Evento: Grabación finalizada. Error code: $error, Uri: $outputUri")

                            // --- CORRECCIÓN: Usar valor numérico 0 en lugar de Recording.ERROR_NONE ---
                            if (error != 0 || outputUri == Uri.EMPTY) {
                                // ----------------------------------------------------------------------
                                Log.e(TAG, "Error al finalizar grabación (${error}): ${recordEvent.cause?.message}")
                                _lastRecordedVideoUri.value = null
                            } else {
                                Log.i(TAG, "Grabación exitosa. Uri guardada: $outputUri")
                                _lastRecordedVideoUri.value = outputUri
                            }
                            activeRecording = null
                            _isRecording.value = false
                        }
                        is VideoRecordEvent.Status -> { /* ... */ }
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Excepción al iniciar grabación", e)
            activeRecording = null
            _isRecording.value = false
            _lastRecordedVideoUri.value = null
        }
    }

    fun stopRecording() {
        val currentRecording = activeRecording
        if (currentRecording != null) {
            Log.d(TAG, "Llamando a stop()...")
            currentRecording.stop()
        } else {
            Log.w(TAG, "Intento de detener grabación no activa.")
            if (_isRecording.value) { _isRecording.value = false }
        }
    }

    fun getLastRecordedVideoUri(): Uri? {
        return _lastRecordedVideoUri.value
    }

    fun getLastCapturedImageUri(): Uri? {
        return _lastCapturedImageUri.value
    }

    fun clearLastRecordedVideoUri() {
        _lastRecordedVideoUri.value = null
    }

    fun clearLastCapturedImageUri() {
        _lastCapturedImageUri.value = null
    }
}