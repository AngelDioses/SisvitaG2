package com.example.sisvitag2.ui.screens.home

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.sisvitag2.R // Asegúrate que R se importe correctamente
import com.example.sisvitag2.data.model.Test // Modelo de datos para Test
import com.example.sisvitag2.ui.screens.test.TestViewModel // ViewModel para la lógica de Tests
import com.example.sisvitag2.ui.theme.SisvitaG2Theme
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeScreen(
    navController: NavController, // Para navegar a otras pantallas
    testViewModel: TestViewModel = koinViewModel(), // Para obtener la lista de tests
    userName: String? // Nombre del usuario logueado, viene del SessionViewModel
) {
    // Observar los datos del TestViewModel
    val testsState by testViewModel.tests.collectAsState()
    val isLoadingTests by testViewModel.isLoadingTests.collectAsState()

    // Log para verificar que userName se recibe correctamente
    LaunchedEffect(userName) {
        Log.d("HomeScreen", "HomeScreen: userName recibido: $userName")
    }

    // Column principal para el contenido de HomeScreen
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()) // Permite scroll si el contenido es largo
            .background(MaterialTheme.colorScheme.background)
            // Padding interno para el contenido de HomeScreen, no el del Scaffold
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp) // Espacio uniforme entre secciones
    ) {
        Welcome(userName = userName)

        TestSection(
            tests = testsState,
            isLoading = isLoadingTests,
            onNavigateToTestList = { navController.navigate("Test") }, // Navega a la lista de tests
            onStartTestClick = { selectedTest ->
                Log.d("HomeScreen", "Iniciando test con ID: ${selectedTest.id}")
                navController.navigate("DoTest/${selectedTest.id}") // Navega para tomar el test
            }
        )

        HelpMeAdd(navController = navController)
    }
}

// --- Composable para el Mensaje de Bienvenida ---
@Composable
fun Welcome(userName: String?) {
    Box(modifier = Modifier.fillMaxWidth()) { // Contenedor para el saludo
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp)) // Bordes redondeados
                .background(MaterialTheme.colorScheme.primaryContainer) // Color de fondo del tema
                .padding(horizontal = 24.dp, vertical = 16.dp) // Padding interno
        ) {
            Text(
                text = if (!userName.isNullOrBlank()) "Hola, $userName!" else "¡Bienvenido/a!",
                style = MaterialTheme.typography.headlineSmall, // Estilo de texto del tema
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer, // Color de texto del tema
            )
            Spacer(modifier = Modifier.height(8.dp)) // Espacio vertical
            Text(
                text = "\"Recuerda, cada paso que das te acerca un poco más a tus metas. ¡Sigue adelante con determinación!\"",
                style = MaterialTheme.typography.bodyMedium,
                fontStyle = FontStyle.Italic, // Cursiva para la cita
                textAlign = TextAlign.Center, // Texto centrado
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                lineHeight = 20.sp // Altura de línea para mejor legibilidad
            )
        }
    }
}

// --- Composable para la Sección de Tests ---
@Composable
fun TestSection(
    tests: List<Test>,
    isLoading: Boolean,
    onNavigateToTestList: () -> Unit,
    onStartTestClick: (Test) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) { // Contenedor para la sección de tests
        Row( // Fila para el título y el botón "Ver todo"
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween, // Espacio entre título y botón
            verticalAlignment = Alignment.CenterVertically // Alineación vertical
        ) {
            Text(
                "Realiza tu Test",
                style = MaterialTheme.typography.titleLarge, // Estilo de título
                color = MaterialTheme.colorScheme.onSurface
            )
            if (!isLoading && tests.isNotEmpty()) { // Mostrar botón solo si hay tests y no está cargando
                TextButton(onClick = onNavigateToTestList) {
                    Text(text = "Ver todo")
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp)) // Espacio

        // Contenido de la sección: indicador de carga, mensaje de no tests, o carrusel
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth().height(200.dp), // Altura fija para el loader
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator() // Indicador de carga
            }
        } else if (tests.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().height(200.dp).padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text( // Mensaje si no hay tests
                    "No hay tests disponibles en este momento.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            // Mostrar solo los primeros N tests en el carrusel de la pantalla de inicio
            TestCarousel(tests = tests.take(3), onTestClick = onStartTestClick) // Muestra hasta 3 tests
        }
    }
}

// --- Composable para el Carrusel de Tests ---
@Composable
fun TestCarousel(tests: List<Test>, onTestClick: (Test) -> Unit) {
    LazyRow( // Fila horizontal con scroll
        contentPadding = PaddingValues(vertical = 8.dp), // Padding vertical para el carrusel
        horizontalArrangement = Arrangement.spacedBy(16.dp) // Espacio entre tarjetas de test
    ) {
        items(tests, key = { it.id }) { test -> // Itera sobre la lista de tests
            TestCard(test = test, onStartClick = { onTestClick(test) }) // Muestra cada tarjeta
        }
    }
}

// --- Composable para la Tarjeta de Test Individual ---
@Composable
fun TestCard(test: Test, onStartClick: () -> Unit) {
    Card(
        modifier = Modifier.width(240.dp).height(200.dp), // Dimensiones de la tarjeta
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp), // Sombra
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant) // Color de fondo
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp), // Padding interno de la tarjeta
            verticalArrangement = Arrangement.SpaceBetween // Empuja el botón hacia abajo
        ) {
            Column { // Contenedor para el nombre y descripción del test
                Text(
                    text = test.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2, // Limita a 2 líneas
                    overflow = TextOverflow.Ellipsis // Añade "..." si el texto es muy largo
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = test.descripcion,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f), // Texto ligeramente transparente
                    maxLines = 3, // Limita a 3 líneas
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp
                )
            }
            Button( // Botón para comenzar el test
                onClick = onStartClick,
                modifier = Modifier.align(Alignment.End), // Alinea el botón a la derecha
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Comenzar")
            }
        }
    }
}

// --- Composable para la Sección "Necesito Ayuda" ---
@Composable
fun HelpMeAdd(navController: NavController) {
    Column(modifier = Modifier.fillMaxWidth()) { // Contenedor para la sección
        Text( // Título de la sección
            text = "¿Sientes ansiedad?",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))
        Card( // Tarjeta para el contenido de "Necesito Ayuda"
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Row( // Fila para imagen y texto/botón
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image( // Icono
                    painter = painterResource(id = R.drawable.ic_psychologisthelp), // Asegúrate que el recurso drawable exista
                    contentDescription = "Ayuda psicológica",
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.width(16.dp)) // Espacio entre imagen y texto
                Column(modifier = Modifier.weight(1f)) { // Columna para texto y botón, toma el espacio restante
                    Text(
                        "Obtén ayuda aquí",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Si te sientes abrumado o ansioso, usa esta opción para hablar con nosotros y recibir orientación.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button( // Botón para ir al flujo de ayuda
                        onClick = { navController.navigate("NecesitoAyudaFlow") }, // Navega al flujo anidado
                        modifier = Modifier.align(Alignment.End), // Alinea el botón a la derecha
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Text("Ir", color = MaterialTheme.colorScheme.onSecondary)
                    }
                }
            }
        }
    }
}

// --- Preview Composable para HomeScreen ---
@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_NO)
@Composable
fun HomeScreenLightPreview() {
    SisvitaG2Theme { // Aplica tu tema
        val navController = rememberNavController() // NavController de prueba para el preview
        // HomeScreen no toma paddingValues directamente; el NavHost se encarga de eso.
        HomeScreen(navController = navController, userName = "Ángel Preview")
    }
}