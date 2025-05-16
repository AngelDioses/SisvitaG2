package com.example.sisvitag2.ui.screens.auth

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack // Para el botón de atrás
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.sisvitag2.ui.vm.ForgotPasswordViewModel
import com.example.sisvitag2.ui.vm.ForgotPasswordState
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    navController: NavController,
    viewModel: ForgotPasswordViewModel = koinViewModel()
) {
    var email by remember { mutableStateOf("") }
    val state by viewModel.forgotPasswordState.collectAsState()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    LaunchedEffect(state) {
        when (val currentState = state) {
            is ForgotPasswordState.Success -> {
                Toast.makeText(context, currentState.message, Toast.LENGTH_LONG).show()
                // Opcional: navegar de vuelta a Login después de un delay o directamente
                // navController.popBackStack()
                viewModel.resetState() // Para permitir reintentos o limpiar mensaje
            }
            is ForgotPasswordState.Error -> {
                Toast.makeText(context, currentState.message, Toast.LENGTH_LONG).show()
                viewModel.resetState()
            }
            else -> { /* No-op para Idle o Loading aquí, se maneja en UI */ }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Restablecer Contraseña") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "Ingresa tu correo electrónico para recibir un enlace y restablecer tu contraseña.",
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 24.dp),
                style = MaterialTheme.typography.bodyLarge
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo Electrónico") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = {
                    focusManager.clearFocus()
                    viewModel.sendPasswordResetEmail(email.trim())
                }),
                enabled = state !is ForgotPasswordState.Loading
            )
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    focusManager.clearFocus()
                    viewModel.sendPasswordResetEmail(email.trim())
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = state !is ForgotPasswordState.Loading
            ) {
                if (state is ForgotPasswordState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Enviar Enlace de Restablecimiento")
                }
            }
        }
    }
}