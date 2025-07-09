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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.delay
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
        Log.d("MainActivity", "=== MAINACTIVITY ONCREATE ===")
        try {
            enableEdgeToEdge()
        } catch (e: Exception) {
            Log.e("MainActivity", "Error en enableEdgeToEdge (puede ser por API level o dependencia): ${e.message}")
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }

        setContent {
            Log.d("MainActivity", "=== SETCONTENT LLAMADO ===")
            SisvitaG2Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val sessionViewModel: SessionViewModel = koinViewModel()
                    val authState by sessionViewModel.authState
                    // Usar un key para forzar recomposición total al cambiar el estado de autenticación
                    androidx.compose.runtime.key(authState) {
                        AuthDecisionRoot()
                    }
                }
            }
        }
    }
}

@Composable
fun AuthDecisionRoot() {
    Log.d("MainActivity", "=== AUTH DECISION ROOT ===")
    val sessionViewModel: SessionViewModel = koinViewModel()
    val authState by sessionViewModel.authState
    val globalNavController = rememberNavController()

    Log.d("AuthDecisionRoot", "Estado de autenticación actual: $authState")

    when (val currentAuthState = authState) {
        is AuthState.Loading -> {
            Log.d("AuthDecisionRoot", "=== ESTADO LOADING ===")
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is AuthState.Authenticated -> {
            Log.d("AuthDecisionRoot", "=== ESTADO AUTHENTICATED ===")
            val user = currentAuthState.user
            
            if (!user.isEmailVerified) {
                Log.d("AuthDecisionRoot", "Usuario ${user.email} autenticado PERO correo no verificado. Mostrando AuthNavHost con inicio en EmailVerificationRoute.")
                AuthNavHost(navController = globalNavController, startDestination = "EmailVerificationRoute")
            } else {
                Log.d("AuthDecisionRoot", "Usuario ${user.email} autenticado Y VERIFICADO. Mostrando AppScaffold.")
                AppScaffoldComponent(
                    onLogout = {
                        sessionViewModel.signOut()
                        // Eliminada la navegación manual a LoginRoute
                    },
                    navController = globalNavController
                )
            }
        }
        is AuthState.Unauthenticated -> {
            Log.d("AuthDecisionRoot", "=== ESTADO UNAUTHENTICATED ===")
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