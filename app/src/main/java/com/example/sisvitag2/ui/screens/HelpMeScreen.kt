package com.example.sisvitag2.ui.screens // Ajusta el paquete si es necesario

import androidx.compose.foundation.ExperimentalFoundationApi // <-- IMPORT NECESARIO
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.sisvitag2.R // Ajusta import de R
import com.example.sisvitag2.ui.theme.SisvitaG2Theme // Ajusta import de Theme

// --- ANOTACIÓN @OptIn para la función que usa PagerState y HorizontalPager ---
@OptIn(ExperimentalFoundationApi::class)
// -------------------------------------------------------------------------
@Composable
fun HelpMeScreen(navController: NavController){
    // PagerState es parte de la API experimental
    val pagerState = rememberPagerState(pageCount = { 4 })

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center
    ) {
        // HorizontalPager es parte de la API experimental
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f) // Permite que ocupe espacio disponible
        ) { page ->
            // Aquí están las llamadas completas a los Step Cards
            when (page) {
                0 -> StepCard(
                    imageResource = R.drawable.ic_step1, // Verifica ID
                    description = "Asegúrate de estar en un lugar bien iluminado y tranquilo."
                )
                1 -> StepCard(
                    imageResource = R.drawable.ic_step2, // Verifica ID
                    description = "Ajusta tu posición para que tu rostro esté centrado en la pantalla."
                )
                2 -> StepCard(
                    imageResource = R.drawable.ic_step3, // Verifica ID
                    description = "Tómate un momento para respirar profundamente y calmarte."
                )
                3 -> StepCardWithCamera(
                    imageResource = R.drawable.ic_step4, // Verifica ID
                    description = "Recuerda que es un espacio seguro para compartir cómo te sientes.",
                    navController = navController // Pasa el NavController
                )
                // Un else por seguridad, aunque con pageCount fijo no debería alcanzarse
                else -> Spacer(Modifier.fillMaxSize())
            }
        }

        // StepIndicator usa pagerState.currentPage y pagerState.pageCount
        StepIndicator(currentStep = pagerState.currentPage + 1, totalSteps = pagerState.pageCount)
    }
}

@Composable
fun StepCard(imageResource: Int, description: String) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 24.dp)
        ) {
            Image(
                painter = painterResource(imageResource),
                contentDescription = description, // Añadir contentDescription
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .aspectRatio(1f) // Mantiene proporción
                    .padding(bottom = 16.dp)
            )
            Text(
                text = description,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun StepCardWithCamera(imageResource: Int, description: String, navController: NavController) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Image(
                    painter = painterResource(imageResource),
                    contentDescription = description, // Añadir contentDescription
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .aspectRatio(1f)
                        .padding(bottom = 16.dp)
                )
                Text(
                    text = description,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(64.dp)) // Espacio para el botón
            }

            IconButton(
                onClick = {
                    navController.navigate("Rec") // Navega a la pantalla de cámara
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_camera), // Verifica ID
                    contentDescription = "Abrir cámara",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

// --- ANOTACIÓN @OptIn para StepIndicator ---
// porque usa pagerState.currentPage y pagerState.pageCount que son parte de la API experimental
@OptIn(ExperimentalFoundationApi::class)
// ---------------------------------------
@Composable
fun StepIndicator(currentStep: Int, totalSteps: Int) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp) // Padding vertical
    ) {
        // Asegura que totalSteps sea al menos 1 para evitar errores en el rango
        if (totalSteps > 0) {
            for (i in 1..totalSteps) {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(
                            if (i == currentStep) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                )
            }
        }
    }
}

// Preview se mantiene igual
@Preview(showBackground = true)
@Composable
fun HelpMePreview() {
    SisvitaG2Theme(darkTheme = false) { // Asegúrate que el nombre del tema sea el correcto
        val navController = rememberNavController()
        HelpMeScreen(navController)
    }
}