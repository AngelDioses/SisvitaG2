package com.example.sisvitag2.data.repository.emotionalAnalysis

import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.util.concurrent.TimeUnit

class EmotionalAnalysisApiService {
    
    companion object {
        private const val TAG = "EmotionalAnalysisApi"
        private const val BASE_URL = "https://detect-emotions-3uyocih3lq-uc.a.run.app"
        private const val TIMEOUT_SECONDS = 60L
    }
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .build()
    
    /**
     * Analiza una imagen para detectar emociones usando la nueva API
     * @param imageUri URI de la imagen a analizar
     * @return EmotionalAnalysisResponse con los resultados
     */
    suspend fun analyzeImage(imageUri: Uri): EmotionalAnalysisResponse = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Iniciando análisis de imagen: $imageUri")
            
            // Convertir URI a File
            val imageFile = uriToFile(imageUri)
            if (!imageFile.exists()) {
                throw IOException("El archivo de imagen no existe: ${imageFile.absolutePath}")
            }
            
            Log.d(TAG, "Archivo de imagen encontrado: ${imageFile.absolutePath}, tamaño: ${imageFile.length()} bytes")
            
            // Crear el cuerpo de la petición multipart
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "file",
                    imageFile.name,
                    imageFile.asRequestBody("image/jpeg".toMediaType())
                )
                .build()
            
            // Crear la petición HTTP
            val request = Request.Builder()
                .url(BASE_URL)
                .post(requestBody)
                .build()
            
            Log.d(TAG, "Enviando petición de imagen a: $BASE_URL")
            
            // Ejecutar la petición
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errorBody = response.body?.string() ?: "Sin detalles de error"
                    Log.e(TAG, "Error en la API: ${response.code} - $errorBody")
                    throw IOException("Error en la API: ${response.code} - $errorBody")
                }
                
                val responseBody = response.body?.string()
                Log.d(TAG, "Respuesta de imagen recibida: $responseBody")
                
                if (responseBody.isNullOrBlank()) {
                    throw IOException("Respuesta vacía de la API")
                }
                
                // Parsear la respuesta JSON
                return@withContext parseResponse(responseBody)
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error durante el análisis de imagen", e)
            throw e
        }
    }

    /**
     * Analiza un video para detectar emociones usando la nueva API
     * @param videoUri URI del video a analizar
     * @return EmotionalAnalysisResponse con los resultados
     */
    suspend fun analyzeVideo(videoUri: Uri): EmotionalAnalysisResponse = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Iniciando análisis de video: $videoUri")
            
            // Convertir URI a File
            val videoFile = uriToFile(videoUri)
            if (!videoFile.exists()) {
                throw IOException("El archivo de video no existe: ${videoFile.absolutePath}")
            }
            
            Log.d(TAG, "Archivo de video encontrado: ${videoFile.absolutePath}, tamaño: ${videoFile.length()} bytes")
            
            // Crear el cuerpo de la petición multipart
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "file",
                    videoFile.name,
                    videoFile.asRequestBody("video/mp4".toMediaType())
                )
                .build()
            
            // Crear la petición HTTP
            val request = Request.Builder()
                .url(BASE_URL)
                .post(requestBody)
                .build()
            
            Log.d(TAG, "Enviando petición a: $BASE_URL")
            
            // Ejecutar la petición
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errorBody = response.body?.string() ?: "Sin detalles de error"
                    Log.e(TAG, "Error en la API: ${response.code} - $errorBody")
                    throw IOException("Error en la API: ${response.code} - $errorBody")
                }
                
                val responseBody = response.body?.string()
                Log.d(TAG, "Respuesta recibida: $responseBody")
                
                if (responseBody.isNullOrBlank()) {
                    throw IOException("Respuesta vacía de la API")
                }
                
                // Parsear la respuesta JSON
                return@withContext parseResponse(responseBody)
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error durante el análisis de video", e)
            throw e
        }
    }
    
    /**
     * Convierte una URI a un archivo File
     */
    private fun uriToFile(uri: Uri): File {
        return when (uri.scheme) {
            "file" -> File(uri.path!!)
            "content" -> {
                // Para URIs de contenido, necesitamos copiar el archivo
                val inputStream = FileInputStream(uri.path!!)
                val tempFile = File.createTempFile("video_analysis_", ".mp4")
                tempFile.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
                inputStream.close()
                tempFile
            }
            else -> throw IOException("URI no soportada: $uri")
        }
    }
    
    /**
     * Parsea la respuesta JSON de la API
     */
    private fun parseResponse(jsonResponse: String): EmotionalAnalysisResponse {
        try {
            // Parsear el JSON manualmente ya que es simple
            val cleanJson = jsonResponse.trim().removeSurrounding("{", "}")
            val pairs = cleanJson.split(",").map { it.trim() }
            
            var angry = 0
            var disgust = 0
            var fear = 0
            var happy = 0
            var sad = 0
            var surprise = 0
            var neutral = 0
            
            pairs.forEach { pair ->
                val (key, value) = pair.split(":").map { it.trim().removeSurrounding("\"") }
                val intValue = value.toIntOrNull() ?: 0
                
                when (key) {
                    "angry" -> angry = intValue
                    "disgust" -> disgust = intValue
                    "fear" -> fear = intValue
                    "happy" -> happy = intValue
                    "sad" -> sad = intValue
                    "surprise" -> surprise = intValue
                    "neutral" -> neutral = intValue
                }
            }
            
            val result = EmotionalAnalysisResponse(
                angry = angry,
                disgust = disgust,
                fear = fear,
                happy = happy,
                sad = sad,
                surprise = surprise,
                neutral = neutral
            )
            
            // IMPRIMIR LOS VALORES EN CONSOLA
            Log.i(TAG, "=== RESULTADOS DE ANÁLISIS DE EMOCIONES ===")
            Log.i(TAG, "JSON Original: $jsonResponse")
            Log.i(TAG, "Angry: $angry")
            Log.i(TAG, "Disgust: $disgust")
            Log.i(TAG, "Fear: $fear")
            Log.i(TAG, "Happy: $happy")
            Log.i(TAG, "Sad: $sad")
            Log.i(TAG, "Surprise: $surprise")
            Log.i(TAG, "Neutral: $neutral")
            Log.i(TAG, "Total de emociones: ${result.getTotalEmotions()}")
            Log.i(TAG, "Emoción dominante: ${result.getDominantEmotion()}")
            
            // Imprimir porcentajes
            val percentages = result.getEmotionPercentages()
            Log.i(TAG, "=== PORCENTAJES ===")
            percentages.forEach { (emotion, percentage) ->
                Log.i(TAG, "$emotion: ${String.format("%.2f", percentage)}%")
            }
            Log.i(TAG, "=== FIN RESULTADOS ===")
            
            return result
            
        } catch (e: Exception) {
            Log.e(TAG, "Error parseando respuesta JSON: $jsonResponse", e)
            throw IOException("Error parseando respuesta de la API: ${e.message}")
        }
    }
} 