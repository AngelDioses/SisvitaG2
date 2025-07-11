package com.example.sisvitag2.ui.screens.specialist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.sisvitag2.data.model.EmotionalAnalysisSubmission
import com.example.sisvitag2.ui.vm.SpecialistViewModel
import org.koin.androidx.compose.koinViewModel
import com.google.firebase.Timestamp

@Composable
fun PendingEmotionalAnalysesScreen(
    navController: NavController,
    specialistViewModel: SpecialistViewModel = koinViewModel()
) {
    val uiState by specialistViewModel.uiState.collectAsState()
    val isLoading = uiState.isLoading
    val error = uiState.error
    val analyses = uiState.pendingEmotionalAnalyses

    var loaded by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (!loaded) {
            specialistViewModel.loadPendingEmotionalAnalyses()
            loaded = true
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            isLoading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            error != null -> {
                Text("Error: $error", color = MaterialTheme.colorScheme.error, modifier = Modifier.align(Alignment.Center))
            }
            analyses.isEmpty() -> {
                Text("No hay análisis emocionales pendientes.", modifier = Modifier.align(Alignment.Center))
            }
            else -> {
                LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    items(analyses) { analysis ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .clickable {
                                    navController.navigate("EmotionalAnalysisDetail/${analysis.id}")
                                },
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Nombre: ${analysis.userName}")
                                Text("Apellido Paterno: ${analysis.apellidoPaterno}")
                                Text("Fecha de nacimiento: " + (
                                    when (val fn = analysis.fechaNacimiento) {
                                        is Timestamp -> java.text.SimpleDateFormat("dd/MM/yyyy").format(fn.toDate())
                                        is java.util.Date -> java.text.SimpleDateFormat("dd/MM/yyyy").format(fn)
                                        is String -> fn
                                        else -> "-"
                                    }
                                ))
                                Text("Enviado: ${analysis.timestamp.toDate().let { java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(it) }}")
                            }
                        }
                    }
                }
            }
        }
    }
} 