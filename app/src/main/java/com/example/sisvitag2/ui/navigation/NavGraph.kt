package com.example.sisvitag2.ui.navigation

import android.util.Log
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.sisvitag2.data.repository.emotionalAnalysis.EmotionalAnalysisResponse
import com.example.sisvitag2.ui.screens.HelpMeScreen
import com.example.sisvitag2.ui.screens.account.AccountScreen
import com.example.sisvitag2.ui.screens.account.EditProfileScreen
import com.example.sisvitag2.ui.screens.account.ChangePasswordScreen
import com.example.sisvitag2.ui.screens.camera.CameraScreen
import com.example.sisvitag2.ui.screens.home.HomeScreen
import com.example.sisvitag2.ui.screens.loading.LoadingScreen
import com.example.sisvitag2.ui.screens.orientation.OrientationScreen
import com.example.sisvitag2.ui.screens.results.ResultsScreen
import com.example.sisvitag2.ui.screens.test.TestScreen
import com.example.sisvitag2.ui.screens.testForm.TestFormScreen
import com.example.sisvitag2.ui.vm.SessionViewModel
import kotlinx.serialization.json.Json
import org.koin.androidx.compose.koinViewModel
import org.koin.androidx.compose.get
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.sisvitag2.ui.screens.account.AdminScreen
import com.example.sisvitag2.ui.screens.specialist.SpecialistHomeScreen
import com.example.sisvitag2.ui.screens.specialist.TestListScreen
import com.example.sisvitag2.ui.screens.specialist.FeedbackHistoryScreen
import com.example.sisvitag2.ui.screens.specialist.SpecialistTestDetailScreen
import com.example.sisvitag2.ui.screens.feedback.FeedbackScreen
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun PendingApprovalScreen() {
    Log.d("NavGraph", "=== PENDINGAPPROVALSCREEN RENDERIZADO ===")
    androidx.compose.material3.Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            androidx.compose.material3.Text(
                text = "Tu cuenta está pendiente de aprobación. Por favor, espera a que el administrador asigne tu rol.",
                style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
fun AccountBlockedScreen() {
    androidx.compose.material3.Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            androidx.compose.material3.Text(
                text = "Tu cuenta ha sido rechazada. Si crees que es un error, contacta al soporte.",
                style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                color = androidx.compose.material3.MaterialTheme.colorScheme.error,
            )
        }
    }
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    paddingValues: PaddingValues,
    startDestination: String = "Inicio"
) {
    Log.d("NavGraph", "=== APPNavHost RENDERIZADO ===")
    val sessionViewModel: SessionViewModel = koinViewModel()
    val userName by sessionViewModel.userName
    val userRol by sessionViewModel.userRol
    val userEstado by sessionViewModel.userEstado
    val authState by sessionViewModel.authState
    
    Log.d("NavGraph", "Valores iniciales en AppNavHost:")
    Log.d("NavGraph", "userName: '$userName'")
    Log.d("NavGraph", "userRol: $userRol")
    Log.d("NavGraph", "userEstado: '$userEstado'")

    // Logging para debug
    LaunchedEffect(userEstado) {
        Log.d("NavGraph", "userEstado cambió a: $userEstado")
    }

    // Redirección global según estado de autenticación
    LaunchedEffect(authState) {
        if (authState is com.example.sisvitag2.ui.vm.AuthState.Unauthenticated) {
            navController.navigate("Login") {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: ""

    LaunchedEffect(userEstado, userRol, currentRoute) {
        Log.d("NavGraph", "=== NAVGRAPH LaunchedEffect userEstado/userRol/currentRoute ===")
        Log.d("NavGraph", "userEstado: '$userEstado'")
        Log.d("NavGraph", "userRol: $userRol")
        Log.d("NavGraph", "currentBackStackEntry (route): $currentRoute")
        Log.d("NavGraph", "userName: '$userName'")
        // Solo navegar si tenemos datos válidos
        if (userEstado == null || userRol == null) {
            Log.d("NavGraph", "userEstado o userRol es null, esperando datos...")
            return@LaunchedEffect
        }
        val pantallasPermitidasPersona = listOf("Inicio", "Test", "DoTest", "Feedbacks", "Cuenta", "Necesito ayuda", "Historial")
        val pantallasPermitidasEspecialista = listOf("Inicio", "Tests Pendientes", "Historial de Feedback", "Cuenta", "DetalleTestEspecialista")
        when (userEstado) {
            "pendiente" -> {
                if (currentRoute != "Pendiente") {
                    navController.navigate("Pendiente") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
            "rechazado" -> {
                if (currentRoute != "Bloqueado") {
                    navController.navigate("Bloqueado") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
            "aprobado" -> {
                when (userRol) {
                    3 -> if (currentRoute != "Inicio") {
                        navController.navigate("Inicio") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                    2 -> if (pantallasPermitidasEspecialista.none { currentRoute.startsWith(it) }) {
                        navController.navigate("Inicio") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                    1 -> if (pantallasPermitidasPersona.none { currentRoute.startsWith(it) }) {
                        navController.navigate("Inicio") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = Modifier.padding(paddingValues)
    ) {
        composable("Inicio") {
            Log.d("NavGraph", "=== COMPOSABLE INICIO RENDERIZADO ===")
            Log.d("NavGraph", "userEstado en Inicio: '$userEstado'")
            Log.d("NavGraph", "userName en Inicio: '$userName'")
            Log.d("NavGraph", "userRol en Inicio: $userRol")
            
            // Mostrar loading si el usuario está autenticado pero los datos aún no están listos
            val isAuthenticated = authState is com.example.sisvitag2.ui.vm.AuthState.Authenticated
            if (isAuthenticated && (userRol == null || userEstado == null)) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    androidx.compose.material3.CircularProgressIndicator()
                }
                return@composable
            } else {
            when (userEstado) {
                "aprobado" -> {
                        Log.d("NavGraph", "Mostrando pantalla según rol para usuario aprobado")
                        when (userRol) {
                            3 -> AdminScreen() // Administrador
                            2 -> SpecialistHomeScreen() // Especialista
                            1 -> HomeScreen(navController = navController) // Persona
                            else -> HomeScreen(navController = navController)
                        }
                }
                "pendiente" -> {
                    Log.d("NavGraph", "Mostrando PendingApprovalScreen para usuario pendiente")
                    PendingApprovalScreen()
                }
                "rechazado" -> {
                    Log.d("NavGraph", "Mostrando AccountBlockedScreen para usuario rechazado")
                    AccountBlockedScreen()
                }
                null -> {
                    Log.d("NavGraph", "userEstado es null, mostrando loading")
                    androidx.compose.material3.CircularProgressIndicator()
                }
                else -> {
                    Log.d("NavGraph", "Estado desconocido '$userEstado', mostrando loading")
                    androidx.compose.material3.CircularProgressIndicator()
                    }
                }
            }
        }
        composable("Pendiente") { 
            Log.d("NavGraph", "=== COMPOSABLE PENDIENTE RENDERIZADO ===")
            PendingApprovalScreen() 
        }
        composable("Bloqueado") { AccountBlockedScreen() }
        composable("Test") {
            if (userRol == 1 && userEstado == "aprobado") {
            TestScreen(
                navController = navController
            )
            } else {
                PendingApprovalScreen()
            }
        }
        composable("Tests Pendientes") {
            if (userRol == 2 && userEstado == "aprobado") {
                TestListScreen(onTestClick = { testId ->
                    navController.navigate("DetalleTestEspecialista/$testId")
                })
            } else {
                PendingApprovalScreen()
            }
        }
        composable(
            route = "DetalleTestEspecialista/{testId}",
            arguments = listOf(navArgument("testId") { type = NavType.StringType })
        ) { backStackEntry ->
            val testId = backStackEntry.arguments?.getString("testId") ?: ""
            SpecialistTestDetailScreen(
                testId = testId,
                onFeedbackSent = { navController.popBackStack() }
            )
        }
        composable("Historial de Feedback") {
            if (userRol == 2 && userEstado == "aprobado") {
                FeedbackHistoryScreen()
            } else {
                PendingApprovalScreen()
            }
        }
        composable("Necesito ayuda") {
            if (userEstado == "aprobado" && userRol == 1) {
            HelpMeNavHost(
                rootNavController = navController
            )
            } else {
                PendingApprovalScreen()
            }
        }
        composable("Historial") {
            // TODO: HistoryScreen(navController = navController)
        }
        composable("Cuenta") {
            if (userEstado == "aprobado") {
            AccountScreen(
                navController = navController // <--- PASAR NavController
            )
            } else {
                PendingApprovalScreen()
            }
        }
        composable(
            route = "DoTest/{testId}",
            arguments = listOf(navArgument("testId") { type = NavType.StringType; nullable = true })
        ) { backStackEntry ->
            if (userEstado == "aprobado") {
            val testId = backStackEntry.arguments?.getString("testId")
            TestFormScreen(
                navController = navController,
                testId = testId
            )
            } else {
                PendingApprovalScreen()
            }
        }
        // ***** NUEVA RUTA PARA EDITAR PERFIL *****
        composable("EditProfileRoute") {
            if (userEstado == "aprobado") {
            EditProfileScreen(navController = navController)
            } else {
                PendingApprovalScreen()
            }
        }
        // ***** RUTA PARA CAMBIAR CONTRASEÑA *****
        composable("ChangePasswordRoute") {
            if (userEstado == "aprobado") {
                ChangePasswordScreen(navController = navController)
            } else {
                PendingApprovalScreen()
            }
        }
        // ***** FIN DE NUEVAS RUTAS *****
        composable("Feedbacks") {
            FeedbackScreen()
        }
    }
}

// HelpMeNavHost (sin cambios respecto a la última versión que te di)
@Composable
fun HelpMeNavHost(
    rootNavController: NavHostController,
) {
    val nestedNavController = rememberNavController()
    val sessionViewModel: SessionViewModel = koinViewModel()
    val userNameForOrientation by sessionViewModel.userName

    NavHost(navController = nestedNavController, startDestination = "Steps") {
        composable("Steps") { HelpMeScreen(nestedNavController) }
        composable("Rec") { CameraScreen(nestedNavController) }
        composable("Loading") { LoadingScreen(nestedNavController) }
        composable(
            route = "results/{videoId}",
            arguments = listOf(navArgument("videoId") { type = NavType.StringType; nullable = true })
        ) { backStackEntry ->
            val videoId = backStackEntry.arguments?.getString("videoId")
            ResultsScreen(
                navController = rootNavController,
                videoId = videoId
            )
        }
        composable(
            route = "Orientations?emotions={emotions}",
            arguments = listOf(navArgument("emotions") { type = NavType.StringType; nullable = true })
        ) { backStackEntry ->
            val encodedEmotions = backStackEntry.arguments?.getString("emotions")
            val emotionsData: EmotionalAnalysisResponse? = encodedEmotions?.let {
                try { Json.decodeFromString<EmotionalAnalysisResponse>(URLDecoder.decode(it, StandardCharsets.UTF_8.toString())) }
                catch (e: Exception) { Log.e("HelpMeNavHost", "Error deserializando emotions: $e"); null }
            }
            OrientationScreen(
                rootNavController = rootNavController,
                userName = userNameForOrientation,
                emotionsData = emotionsData
            )
        }
    }
}