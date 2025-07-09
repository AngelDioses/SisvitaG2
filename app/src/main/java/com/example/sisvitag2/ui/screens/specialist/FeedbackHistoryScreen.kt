package com.example.sisvitag2.ui.screens.specialist

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sisvitag2.ui.vm.SpecialistViewModel
import org.koin.androidx.compose.koinViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import android.util.Log
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackHistoryScreen(
    specialistViewModel: SpecialistViewModel = koinViewModel()
) {
    LaunchedEffect(Unit) {
        Log.d("FeedbackHistoryScreen", "Llamando a loadFeedbackHistory() desde FeedbackHistoryScreen")
        specialistViewModel.loadFeedbackHistory()
    }
    val feedbacks by specialistViewModel.feedbackHistory.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    var testNames by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var userNames by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    // Obtener nombres de tests y usuarios al cargar feedbacks
    LaunchedEffect(feedbacks) {
        val firestore = FirebaseFirestore.getInstance()
        val testIds = feedbacks.map { it.testType }.distinct()
        val userIds = feedbacks.map { it.userId }.distinct()
        val namesMap = mutableMapOf<String, String>()
        val userMap = mutableMapOf<String, String>()
        testIds.forEach { testId ->
            firestore.collection("tests").document(testId).get()
                .addOnSuccessListener { doc ->
                    doc.getString("nombre")?.let { name ->
                        namesMap[testId] = name
                        testNames = namesMap.toMap()
                    }
                }
        }
        userIds.forEach { userId ->
            firestore.collection("usuarios").document(userId).get()
                .addOnSuccessListener { doc ->
                    val nombre = doc.getString("nombre") ?: ""
                    val apellido = doc.getString("apellidopaterno") ?: ""
                    userMap[userId] = "$nombre $apellido"
                    userNames = userMap.toMap()
                }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Historial de Feedback",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Text(
                    text = "Historial de Feedback Enviado",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                if (feedbacks.isEmpty()) {
                    Text("No has enviado feedbacks aún.")
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(feedbacks) { feedback ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp, horizontal = 4.dp),
                                elevation = CardDefaults.cardElevation(2.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Fecha: ${feedback.feedbackDate.toDate()}", style = MaterialTheme.typography.bodySmall)
                                    Text("Usuario: ${userNames[feedback.userId] ?: feedback.userName}", style = MaterialTheme.typography.bodyMedium)
                                    Text("Test: ${testNames[feedback.testType] ?: feedback.testType}", style = MaterialTheme.typography.bodyMedium)
                                    Text("Evaluación: ${feedback.assessment}", style = MaterialTheme.typography.bodySmall)
                                    if (feedback.recommendations.isNotEmpty()) {
                                        Text("Recomendaciones:", style = MaterialTheme.typography.bodySmall)
                                        feedback.recommendations.forEach { rec ->
                                            Text("• $rec", style = MaterialTheme.typography.bodySmall)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
} 