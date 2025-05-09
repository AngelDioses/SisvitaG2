package com.example.sisvitag2.ui.screens.loading // Ajusta paquete

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.sisvitag2.R
import com.example.sisvitag2.ui.components.LottieAnimationComponent
import com.example.sisvitag2.ui.theme.SisvitaG2Theme

import org.koin.androidx.compose.koinViewModel
import java.math.RoundingMode

@Composable
fun LoadingScreen(
    navController: NavController,
    viewModel: LoadingViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val analysisState by viewModel.analysisState.collectAsState()
    var hasNavigatedOrShownError by remember(analysisState) { mutableStateOf(false) }

    // Reacciona a los cambios de estado del ViewModel
    LaunchedEffect(analysisState) {
        if (!hasNavigatedOrShownError) {
            when (val state = analysisState) {
                is AnalysisState.AnalysisSuccess -> {
                    hasNavigatedOrShownError = true
                    Log.d("LoadingScreen", "Análisis exitoso. Navegando a resultados con videoId: ${state.videoId}")

                    navController.navigate("results/${state.videoId}") { // Usa la ruta con argumento
                        popUpTo("Loading") { inclusive = true }
                        launchSingleTop = true
                    }

                }
                is AnalysisState.Error -> {
                    hasNavigatedOrShownError = true
                    Log.e("LoadingScreen", "Error en análisis: ${state.message}")
                    Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                    navController.popBackStack()
                    viewModel.resetState()
                }
                else -> { /* No hacer nada en otros estados aquí */ }
            }
        }
    }

    val statusMessage = when (analysisState) {
        is AnalysisState.Uploading -> "Subiendo video..."
        is AnalysisState.Processing -> "Analizando resultados..." // Mensaje más específico
        is AnalysisState.AnalysisSuccess -> "Análisis completado..."
        is AnalysisState.Error -> "Error en el análisis."
        is AnalysisState.Idle -> "Iniciando..."
        else -> "Cargando..."
    }

    // Muestra la vista de carga
    LoadingView(statusMessage = statusMessage)
}

@Composable
fun LoadingView(statusMessage: String = "Analizando...") {
    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LottieAnimationComponent(
            resId = R.raw.loading,
            modifier = Modifier.size(150.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = statusMessage,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun LoadingViewPreview() {
    SisvitaG2Theme(darkTheme = false) {
        LoadingView(statusMessage = "Analizando previsualización...")
    }
}