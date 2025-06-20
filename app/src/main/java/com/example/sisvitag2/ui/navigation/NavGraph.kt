package com.example.sisvitag2.ui.navigation

import android.util.Log
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
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
import com.example.sisvitag2.ui.screens.account.EditProfileScreen // <-- IMPORTAR EditProfileScreen
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
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

@Composable
fun AppNavHost(
    navController: NavHostController,
    paddingValues: PaddingValues,
    startDestination: String = "Inicio"
) {
    val sessionViewModel: SessionViewModel = koinViewModel()
    val userName by sessionViewModel.userName.collectAsState()

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = Modifier.padding(paddingValues)
    ) {
        composable("Inicio") {
            HomeScreen(
                navController = navController,
                userName = userName
            )
        }
        composable("Test") {
            TestScreen(
                navController = navController
            )
        }
        composable("Necesito ayuda") {
            HelpMeNavHost(
                rootNavController = navController
            )
        }
        composable("Historial") {
            // TODO: HistoryScreen(navController = navController)
        }
        composable("Cuenta") {
            AccountScreen(
                navController = navController // <--- PASAR NavController
            )
        }
        composable(
            route = "DoTest/{testId}",
            arguments = listOf(navArgument("testId") { type = NavType.StringType; nullable = true })
        ) { backStackEntry ->
            val testId = backStackEntry.arguments?.getString("testId")
            TestFormScreen(
                navController = navController,
                testId = testId
            )
        }
        // ***** NUEVA RUTA PARA EDITAR PERFIL *****
        composable("EditProfileRoute") {
            EditProfileScreen(navController = navController)
        }
        // ***** FIN DE NUEVA RUTA *****
    }
}

// HelpMeNavHost (sin cambios respecto a la última versión que te di)
@Composable
fun HelpMeNavHost(
    rootNavController: NavHostController,
) {
    val nestedNavController = rememberNavController()
    val sessionViewModel: SessionViewModel = koinViewModel()
    val userNameForOrientation by sessionViewModel.userName.collectAsState()

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