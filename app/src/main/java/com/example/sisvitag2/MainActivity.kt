package com.example.sisvitag2

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.sisvitag2.ui.components.AppScaffoldComponent
// AppNavHost es llamado desde AppScaffoldComponent, no directamente aquí
// import com.example.sisvitag2.ui.navigation.AppNavHost
import com.example.sisvitag2.ui.screens.auth.EmailVerificationScreen
import com.example.sisvitag2.ui.screens.auth.ForgotPasswordScreen
import com.example.sisvitag2.ui.screens.login.LoginScreen
import com.example.sisvitag2.ui.screens.register.RegisterScreen
import com.example.sisvitag2.ui.theme.SisvitaG2Theme
import com.example.sisvitag2.ui.vm.AuthState
import com.example.sisvitag2.ui.vm.SessionViewModel
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        Log.d("MainActivity", "Permiso de cámara concedido: $isGranted")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            enableEdgeToEdge()
        } catch (e: Exception) {
            Log.e("MainActivity", "Error en enableEdgeToEdge (puede ser por API level o dependencia): ${e.message}")
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }

        setContent {
            SisvitaG2Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AuthDecisionRoot()
                }
            }
        }
    }
}

@Composable
fun AuthDecisionRoot() {
    val sessionViewModel: SessionViewModel = koinViewModel()
    val authState by sessionViewModel.authState.collectAsState()
    val globalNavController = rememberNavController()

    Log.d("AuthDecisionRoot", "Estado de autenticación actual: $authState")

    when (val currentAuthState = authState) {
        is AuthState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is AuthState.Authenticated -> {
            if (!currentAuthState.user.isEmailVerified) {
                Log.d("AuthDecisionRoot", "Usuario ${currentAuthState.user.email} autenticado PERO correo no verificado. Mostrando AuthNavHost con inicio en EmailVerificationRoute.")
                AuthNavHost(navController = globalNavController, startDestination = "EmailVerificationRoute")
            } else {
                Log.d("AuthDecisionRoot", "Usuario ${currentAuthState.user.email} autenticado Y VERIFICADO. Mostrando AppScaffold.")
                val userName by sessionViewModel.userName.collectAsState()
                AppScaffoldComponent(
                    userName = userName ?: "Bienvenido/a",
                    onLogout = { sessionViewModel.signOut() },
                    navController = globalNavController
                )
                // AppNavHost es llamado DENTRO de AppScaffoldComponent
            }
        }
        is AuthState.Unauthenticated -> {
            Log.d("AuthDecisionRoot", "Usuario no autenticado. Mostrando AuthNavHost con inicio en LoginRoute.")
            AuthNavHost(navController = globalNavController, startDestination = "LoginRoute")
        }
    }
}

@Composable
fun AuthNavHost(navController: NavHostController, startDestination: String = "LoginRoute") {
    NavHost(navController = navController, startDestination = startDestination) {
        composable("LoginRoute") {
            LoginScreen(
                navController = navController,
                onLoginSuccess = {
                    Log.d("AuthNavHost", "LoginScreen reportó éxito. AuthDecisionRoot reaccionará.")
                }
            )
        }
        composable("RegisterRoute") {
            RegisterScreen(navController = navController)
        }
        composable("EmailVerificationRoute") {
            EmailVerificationScreen(navController = navController)
        }
        composable("ForgotPasswordRoute") {
            ForgotPasswordScreen(navController = navController)
        }
    }
}