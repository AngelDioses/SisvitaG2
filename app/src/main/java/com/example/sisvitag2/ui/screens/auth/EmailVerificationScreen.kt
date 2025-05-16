package com.example.sisvitag2.ui.screens.auth // Ajusta el paquete

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.sisvitag2.ui.vm.EmailVerificationViewModel
import com.example.sisvitag2.ui.vm.EmailVerificationState
import org.koin.androidx.compose.koinViewModel

@Composable
fun EmailVerificationScreen(
    navController: NavController, // Para navegar a Home o Login si falla mucho
    viewModel: EmailVerificationViewModel = koinViewModel()
) {
    val verificationState by viewModel.verificationState.collectAsState()
    val context = LocalContext.current

    // Enviar correo de verificación automáticamente al entrar si está en Idle
    LaunchedEffect(Unit) {
        if (viewModel.verificationState.value is EmailVerificationState.Idle) {
            viewModel.sendVerificationEmail()
        }
    }

    // Reaccionar a cambios de estado para navegar
    LaunchedEffect(verificationState) {
        when (verificationState) {
            is EmailVerificationState.Verified -> {
                Toast.makeText(context, "¡Correo verificado con éxito!", Toast.LENGTH_SHORT).show()
                // Navegar a la pantalla principal o de login para que el flujo normal continúe
                navController.navigate("LoginRoute") { // O "Inicio" si SessionViewModel lo permite
                    popUpTo(navController.graph.id) { inclusive = true } // Limpiar backstack
                }
                viewModel.resetState() // Limpiar estado del VM
            }
            is EmailVerificationState.Error -> {
                Toast.makeText(context, (verificationState as EmailVerificationState.Error).message, Toast.LENGTH_LONG).show()
                // Podrías permitir al usuario volver a Login o intentar reenviar
            }
            else -> { /* No-op */ }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Verifica tu Correo Electrónico",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        when (val state = verificationState) {
            is EmailVerificationState.Idle, is EmailVerificationState.Sending -> {
                CircularProgressIndicator()
                Text("Enviando correo de verificación...", modifier = Modifier.padding(top = 8.dp))
            }
            is EmailVerificationState.Sent -> {
                Text(
                    text = "Se ha enviado un correo de verificación a ${state.email}. Por favor, revisa tu bandeja de entrada (y spam) y sigue el enlace para continuar.",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
                Button(onClick = { viewModel.sendVerificationEmail() }) {
                    Text("Reenviar Correo")
                }
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(onClick = { viewModel.checkEmailVerificationStatus() }) {
                    Text("Ya lo verifiqué, comprobar ahora")
                }
            }
            is EmailVerificationState.Checking -> {
                CircularProgressIndicator()
                Text("Comprobando estado de verificación...", modifier = Modifier.padding(top = 8.dp))
            }
            is EmailVerificationState.Error -> {
                Text(
                    text = "Error: ${state.message}",
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
                Button(onClick = { viewModel.sendVerificationEmail() }) {
                    Text("Reintentar Enviar Correo")
                }
            }
            is EmailVerificationState.Verified -> {
                Text("¡Correo verificado! Redirigiendo...", textAlign = TextAlign.Center)
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        TextButton(onClick = {
            // Permitir al usuario cerrar sesión o volver al login si se atasca
            viewModel.resetState() // Limpia el estado antes de navegar
            navController.navigate("LoginRoute") {
                popUpTo(navController.graph.id) { inclusive = true }
            }
        }) {
            Text("Volver a Inicio de Sesión")
        }
    }
}