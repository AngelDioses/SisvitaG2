package com.example.sisvitag2.ui.screens.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle // Para feedback disponible
import androidx.compose.material.icons.filled.HourglassEmpty // Para pendiente de feedback
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.sisvitag2.data.model.FeedbackDetallado
import com.example.sisvitag2.data.model.HistorialItemPaciente
import com.example.sisvitag2.data.model.HistorialTipo
import com.example.sisvitag2.ui.theme.SisvitaG2Theme
import com.example.sisvitag2.ui.vm.HistoryUiState
import com.example.sisvitag2.ui.vm.HistoryViewModel
import com.google.firebase.Timestamp
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showFeedbackDialog by remember { mutableStateOf<HistorialItemPaciente?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Historial de Actividades") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading && uiState.historialItems.isEmpty() -> { // Mostrar solo al cargar inicialmente
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                uiState.error != null -> {
                    Text(
                        text = "Error: ${uiState.error}",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center).padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                }
                uiState.historialItems.isEmpty() && !uiState.isLoading -> {
                    Text(
                        text = "No has realizado actividades en los últimos 30 días.",
                        modifier = Modifier.align(Alignment.Center).padding(16.dp),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.historialItems, key = { it.id + it.tipo.name }) { item ->
                            HistorialItemCardPaciente(
                                item = item,
                                onClick = {
                                    if (item.tieneFeedback) {
                                        showFeedbackDialog = item
                                        viewModel.loadFeedbackForItem(item)
                                    }
                                    // Si no tiene feedback, podrías mostrar un Toast
                                }
                            )
                        }
                    }
                }
            }

            // Diálogo para mostrar el feedback
            showFeedbackDialog?.let { item ->
                val feedback = uiState.selectedItemFeedback
                val isLoadingFeedback = uiState.isLoadingFeedback
                FeedbackDisplayDialog(
                    item = item,
                    feedback = feedback,
                    isLoading = isLoadingFeedback,
                    onDismiss = {
                        showFeedbackDialog = null
                        viewModel.clearSelectedFeedback()
                    }
                )
            }
        }
    }
}

@Composable
fun HistorialItemCardPaciente(item: HistorialItemPaciente, onClick: () -> Unit) {
    val sdf = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
    val fechaFormateada = remember(item.fechaRealizacion) { sdf.format(item.fechaRealizacion.toDate()) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.nombreActividad, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text("Realizado: $fechaFormateada", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                item.puntaje?.let {
                    Text("Puntaje: $it ${item.diagnosticoTextoBreve?.let { d -> "($d)" } ?: ""}", style = MaterialTheme.typography.bodySmall)
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = if (item.tieneFeedback) Icons.Filled.CheckCircle else Icons.Filled.HourglassEmpty,
                contentDescription = if (item.tieneFeedback) "Feedback disponible" else "Feedback pendiente",
                tint = if (item.tieneFeedback) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun FeedbackDisplayDialog(
    item: HistorialItemPaciente,
    feedback: FeedbackDetallado?,
    isLoading: Boolean,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Feedback del Especialista para: ${item.nombreActividad}") },
        text = {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (feedback != null) {
                Column {
                    feedback.fechaFeedback?.let {
                        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                        Text("Fecha del Feedback: ${sdf.format(it.toDate())}", style = MaterialTheme.typography.labelSmall)
                        Spacer(Modifier.height(8.dp))
                    }
                    Text("Observación:", fontWeight = FontWeight.Bold)
                    Text(feedback.observacion ?: "No hay observaciones.")
                    Spacer(Modifier.height(8.dp))
                    Text("Recomendación:", fontWeight = FontWeight.Bold)
                    Text(feedback.recomendacion ?: "No hay recomendaciones.")
                }
            } else {
                Text("El feedback para esta actividad aún no está disponible o no se encontró.")
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cerrar") }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun HistoryScreenPreview() {
    val previewItems = listOf(
        HistorialItemPaciente(id = "1", tipo = HistorialTipo.TEST_PSICOLOGICO, nombreActividad = "Test de Ansiedad", fechaRealizacion = Timestamp.now(), tieneFeedback = true, puntaje = 15, diagnosticoTextoBreve = "Ansiedad Leve"),
        HistorialItemPaciente(id = "2", tipo = HistorialTipo.ANALISIS_EMOCIONAL_VIDEO, nombreActividad = "Análisis Facial 10/05", fechaRealizacion = Timestamp(System.currentTimeMillis() - 2*86400000L,0), tieneFeedback = false)
    )
    val previewState = HistoryUiState(historialItems = previewItems)
    SisvitaG2Theme {
        // Para previsualizar directamente el contenido sin el ViewModel y Scaffold completo
        // Es difícil previsualizar el Scaffold completo con estado aquí.
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(previewState.historialItems) { item ->
                HistorialItemCardPaciente(item = item, onClick = {})
            }
        }
    }
}