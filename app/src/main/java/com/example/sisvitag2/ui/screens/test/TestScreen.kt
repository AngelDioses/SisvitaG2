package com.example.sisvitag2.ui.screens.test

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.sisvitag2.data.model.Test
import com.example.sisvitag2.ui.theme.SisvitaG2Theme
import org.koin.androidx.compose.koinViewModel

@Composable
fun TestScreen(
    // paddingValues: PaddingValues, // ELIMINADO
    navController: NavController,
    viewModel: TestViewModel = koinViewModel()
) {
    val tests by viewModel.tests.collectAsState() // Usa el StateFlow individual
    val isLoading by viewModel.isLoadingTests.collectAsState() // Usa el StateFlow individual

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            // El NavHost que contiene esta pantalla ya aplica el padding del Scaffold.
            // Añadir padding interno aquí si es específico para esta pantalla.
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TestListContent(
            tests = tests,
            isLoading = isLoading,
            onStartTestClick = { selectedTest ->
                try {
                    // viewModel.selectTest(selectedTest.id) // Marcar el test seleccionado en el VM
                    // Esto podría ser útil si TestFormScreen lo lee de ahí.
                    // O simplemente navegar con el ID.
                    Log.d("TestScreen", "Navegando a DoTest con ID: ${selectedTest.id}")
                    navController.navigate("DoTest/${selectedTest.id}") // <--- NAVEGAR CON ID
                } catch (e: Exception) {
                    Log.e("TestScreen", "Error al navegar para iniciar test: ${e.message}")
                }
            }
        )
    }
}

// TestListContent, TestCard y Previews como te los di antes (ya estaban bien sin paddingValues).
// ... (Pega aquí el resto de tu código de TestScreen.kt: TestListContent, TestCard, Previews)
@Composable
fun TestListContent(
    tests: List<Test>,
    isLoading: Boolean,
    onStartTestClick: (Test) -> Unit
) {
    Spacer(modifier = Modifier.height(16.dp))
    Text(
        text = "Explora nuestros tests y encuentra el más adecuado para ti.",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(8.dp))

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = MaterialTheme.colorScheme.primary
            )
        }
    } else if (tests.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "No hay tests disponibles.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(tests, key = { it.id }) { test ->
                TestCard(
                    test = test,
                    onStartClick = { onStartTestClick(test) }
                )
            }
        }
    }
}

@Composable
fun TestCard(
    test: Test,
    onStartClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 160.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = test.nombre,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = test.descripcion,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onStartClick,
                modifier = Modifier.align(Alignment.End),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Comenzar Test")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TestListContentPreview() {
    val tests = listOf(
        Test(id = "1", nombre = "Test de Ansiedad Muy Largo Para Probar Ellipsis", descripcion = "Evalúa el nivel de ansiedad general en diversas situaciones de la vida cotidiana y laboral."),
        Test(id = "2", nombre = "Test de Depresión", descripcion = "Mide la severidad de los síntomas depresivos en las últimas dos semanas."),
        Test(id = "3", nombre = "Test de Estrés Percibido", descripcion = "Evalúa cuánto estrés has sentido.")
    )
    SisvitaG2Theme(darkTheme = false) {
        TestListContent(tests = tests, isLoading = false, onStartTestClick = {})
    }
}

@Preview(showBackground = true)
@Composable
fun TestListContentLoadingPreview() {
    SisvitaG2Theme(darkTheme = false) {
        TestListContent(tests = emptyList(), isLoading = true, onStartTestClick = {})
    }
}

@Preview(showBackground = true)
@Composable
fun TestCardPreview() {
    val test = Test(id = "1", nombre = "Test de Ansiedad", descripcion = "Evalúa el nivel de ansiedad general en diversas situaciones.")
    SisvitaG2Theme(darkTheme = false) {
        Box(modifier = Modifier.padding(16.dp)) {
            TestCard(test = test, onStartClick = {})
        }
    }
}