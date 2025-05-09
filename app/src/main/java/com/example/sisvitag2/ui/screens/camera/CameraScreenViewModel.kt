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
import java.io.File
import android.util.Log
import androidx.camera.video.Recording // Mantener el import para la clase Recording
import androidx.camera.video.VideoRecordEvent
import androidx.core.content.ContextCompat
import androidx.core.net.toUri

class CameraScreenViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "CameraScreenViewModel"
    }

    private val app = application
    private var camera: Camera? = null
    private var activeRecording: Recording? = null
    private val _lastRecordedVideoUri = MutableStateFlow<Uri?>(null)
    private val _isFlashOn = MutableStateFlow(false)
    val isFlashOn: StateFlow<Boolean> = _isFlashOn.asStateFlow()
    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean>  = _isRecording.asStateFlow()
    private val _isUsingFrontCamera = MutableStateFlow(false)
    val isUsingFrontCamera: StateFlow<Boolean> = _isUsingFrontCamera.asStateFlow()


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


    @SuppressLint("MissingPermission")
    fun startRecording(context: Context, videoCapture: VideoCapture<Recorder>) {
        if (activeRecording != null || _isRecording.value) { Log.w(TAG, "Grabación ya en progreso."); return }
        val videoFile = File(context.cacheDir, "temp_video_${System.currentTimeMillis()}.mp4")
        Log.d(TAG, "Preparando grabación en: ${videoFile.absolutePath}")
        _lastRecordedVideoUri.value = null
        _isRecording.value = true

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

    fun clearLastRecordedVideoUri() {
        _lastRecordedVideoUri.value = null
    }
}