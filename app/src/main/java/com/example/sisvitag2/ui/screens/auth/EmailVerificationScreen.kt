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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.CheckCircle
import android.app.Activity
import kotlinx.coroutines.delay
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedButton

@Composable
fun EmailVerificationScreen(
    navController: NavController,
    viewModel: EmailVerificationViewModel = koinViewModel(),
    sessionViewModel: com.example.sisvitag2.ui.vm.SessionViewModel = koinViewModel()
) {
    val verificationState by viewModel.verificationState.collectAsState()
    val context = LocalContext.current
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showRestartDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (viewModel.verificationState.value is EmailVerificationState.Idle) {
            viewModel.sendVerificationEmail()
        }
    }

    LaunchedEffect(verificationState) {
        when (verificationState) {
            is EmailVerificationState.Verified -> {
                Toast.makeText(context, "¡Correo verificado con éxito!", Toast.LENGTH_SHORT).show()
                navController.navigate("LoginRoute") {
                    popUpTo(navController.graph.id) { inclusive = true }
                }
                viewModel.resetState()
            }
            is EmailVerificationState.Error -> {
                Toast.makeText(context, (verificationState as EmailVerificationState.Error).message, Toast.LENGTH_LONG).show()
            }
            else -> { }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        when (verificationState) {
            is EmailVerificationState.Verified -> {
                showSuccessDialog = true
            }
            else -> {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                Text(
                            text = "Te enviamos un correo de verificación. Revisa tu bandeja de entrada y haz clic en el enlace.",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = {
                                viewModel.checkEmailVerificationStatus()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Ya verifiqué mi correo")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = { viewModel.sendVerificationEmail() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Reenviar correo")
                }
                        if (verificationState is EmailVerificationState.Error) {
                Spacer(modifier = Modifier.height(16.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.errorContainer,
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Text(
                                    text = (verificationState as EmailVerificationState.Error).message,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.padding(12.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { /* No se puede cerrar tocando fuera */ },
            confirmButton = {
                Button(onClick = { showSuccessDialog = false }) {
                    Text("Entendido")
                }
            },
            title = { Text("¡Verificación exitosa!") },
            text = { Text("Cierra la aplicación e inicia sesión para aplicar los cambios.") }
        )
    }

    // Mostrar AlertDialog solo si el correo está verificado
    if (verificationState is EmailVerificationState.Verified || showRestartDialog) {
        AlertDialog(
            onDismissRequest = {},
            confirmButton = {
                Button(onClick = { showRestartDialog = false }) {
                    Text("OK")
                }
            },
            title = { Text("Verificación exitosa") },
            text = { Text("Para iniciar sesión, reinicie la aplicación") }
        )
    }
    // Cuando el estado cambia a Verificado, mostrar el dialog
    LaunchedEffect(verificationState) {
        if (verificationState is EmailVerificationState.Verified) {
            showRestartDialog = true
        }
    }
}