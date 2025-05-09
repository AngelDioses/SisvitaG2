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
import androidx.navigation.compose.NavHost // Para AuthNavHost
import androidx.navigation.compose.composable // Para AuthNavHost
import androidx.navigation.compose.rememberNavController
import com.example.sisvitag2.ui.components.AppScaffoldComponent // TU Scaffold personalizado
// import com.example.sisvitag2.ui.navigation.AppNavHost // AppNavHost se llama DENTRO de AppScaffoldComponent
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
        enableEdgeToEdge()

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
    val globalNavController = rememberNavController() // NavController para toda la app

    Log.d("AuthDecisionRoot", "Estado de autenticación actual: $authState")

    when (val currentAuthState = authState) {
        is AuthState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is AuthState.Authenticated -> {
            val userName by sessionViewModel.userName.collectAsState()
            // Usuario autenticado: Muestra TU AppScaffoldComponent
            // AppScaffoldComponent ya se encarga de llamar a AppNavHost en su 'content'
            AppScaffoldComponent( // <--- LLAMADA A TU AppScaffoldComponent
                userName = userName ?: "Bienvenido/a",
                onLogout = { sessionViewModel.signOut() },
                navController = globalNavController // El NavController que AppScaffold usará para AppNavHost
            )
            // El lambda de contenido ya no se pasa aquí porque tu AppScaffoldComponent
            // lo maneja internamente llamando a AppNavHost.
        }
        is AuthState.Unauthenticated -> {
            AuthNavHost(navController = globalNavController)
        }
    }
}

@Composable
fun AuthNavHost(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "LoginRoute") {
        composable("LoginRoute") {
            LoginScreen(
                navController = navController,
                onLoginSuccess = {
                    Log.d("AuthNavHost", "LoginScreen reportó éxito.")
                    // No es necesario navegar explícitamente aquí si AuthDecisionRoot reacciona al cambio de AuthState
                }
            )
        }
        composable("RegisterRoute") {
            RegisterScreen(navController = navController)
        }
    }
}