package com.example.sisvitag2.ui.screens.login

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
// import androidx.compose.ui.graphics.Color // No se usa explícitamente
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.sisvitag2.R
import com.example.sisvitag2.data.repository.LoginError
import com.example.sisvitag2.ui.theme.SisvitaG2Theme
import com.example.sisvitag2.ui.theme.philosopherBold
import org.koin.androidx.compose.koinViewModel

@Composable
fun LoginScreen(
    navController: NavController,
    onLoginSuccess: () -> Unit = {}
) {
    val viewModel: LoginViewModel = koinViewModel()
    val loginUiState by viewModel.loginUiState.collectAsState()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val passwordFocusRequester = remember { FocusRequester() }

    LaunchedEffect(loginUiState) {
        when (val state = loginUiState) {
            is LoginUiState.Success -> {
                Log.d("LoginScreen", "Login Success UI detectado por LoginScreen")
                onLoginSuccess()
                viewModel.resetState()
            }
            is LoginUiState.Error -> {
                val errorMessage = when (state.errorType) {
                    LoginError.USER_NOT_FOUND -> "Usuario no encontrado."
                    LoginError.WRONG_PASSWORD -> "Contraseña incorrecta."
                    LoginError.EMPTY_CREDENTIALS -> "Por favor, ingrese correo y contraseña."
                    LoginError.NETWORK_ERROR -> "Error de red. Verifique su conexión."
                    LoginError.USER_DISABLED -> "Esta cuenta ha sido deshabilitada."
                    LoginError.INVALID_CREDENTIALS -> "Credenciales inválidas."
                    else -> state.message ?: "Error desconocido al iniciar sesión."
                }
                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                viewModel.resetState()
            }
            else -> { /* No-op */ }
        }
    }

    val isLoading = loginUiState is LoginUiState.Loading

    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize()
            .padding(horizontal = 32.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_sisvita),
            contentDescription = "Logo Sisvita",
            modifier = Modifier.height(150.dp)
        )
        Text(
            text = "SISVITA",
            style = TextStyle(
                fontFamily = philosopherBold,
                fontSize = 32.sp,
                color = MaterialTheme.colorScheme.primary
            )
        )
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo electrónico") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { passwordFocusRequester.requestFocus() }),
            enabled = !isLoading
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth().focusRequester(passwordFocusRequester),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                focusManager.clearFocus()
                if (!isLoading) { viewModel.login(email.trim(), password) }
            }),
            enabled = !isLoading
        )
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "¿Olvidaste tu contraseña?",
            color = MaterialTheme.colorScheme.primary,
            fontSize = 14.sp,
            modifier = Modifier
                .align(Alignment.End)
                .clickable(enabled = !isLoading) {
                    navController.navigate("ForgotPasswordRoute") // NAVEGAR A OLVIDÉ CONTRASEÑA
                }
                .padding(vertical = 8.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                focusManager.clearFocus()
                if (!isLoading) { viewModel.login(email.trim(), password) }
            },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
            } else {
                Text("Iniciar Sesión", fontSize = 16.sp)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "¿No tienes una cuenta? Regístrate",
            color = MaterialTheme.colorScheme.primary,
            fontSize = 14.sp,
            modifier = Modifier
                .clickable(enabled = !isLoading) {
                    navController.navigate("RegisterRoute")
                }
                .padding(8.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    SisvitaG2Theme {
        LoginScreen(
            navController = rememberNavController(),
            onLoginSuccess = {}
        )
    }
}