package com.example.sisvitag2.ui.components


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.sisvitag2.R
import com.example.sisvitag2.ui.navigation.AppNavHost
import com.example.sisvitag2.ui.theme.SisvitaG2Theme
import kotlinx.coroutines.launch

@Composable
fun AppScaffoldComponent(
    userName: String,
    onLogout: () -> Unit,
    navController: NavHostController
) {
    // Estado del NavigationDrawer
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Estado para la pantalla seleccionada
    var selectedScreen by remember { mutableStateOf("Inicio") }

    // Observar el estado de la ruta actual y actualizar el selectedScreen
    LaunchedEffect(navController) {
        navController.currentBackStackEntryFlow.collect { backStackEntry ->
            selectedScreen = when (backStackEntry.destination.route) {
                "Inicio" -> "Inicio"
                "Test" -> "Test"
                "Necesito ayuda" -> "Necesito Ayuda"
                "Historial" -> "Historial"
                "Cuenta" -> "Cuenta"
                "DoTest" -> "Test"
                else -> "Inicio"
            }
        }
    }

    // Composición del layout con el NavigationDrawer y el TopBar
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            NavigationDrawerComponent(
                userName = userName,
                selectedScreen = selectedScreen,
                onItemClick = { screen ->
                    selectedScreen = screen
                    scope.launch { drawerState.close() } // Cerrar el drawer al seleccionar una opción
                    navController.navigate(screen) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onLogout = onLogout
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopBarComponent(
                    title = if (selectedScreen == "Inicio") "Sisvita" else selectedScreen,
                    onMenuClick = {
                        scope.launch { drawerState.open() } // Abrir el drawer al hacer clic en el botón de menú
                    }
                )
            },
            content = { paddingValues ->
                AppNavHost(navController = navController, paddingValues = paddingValues,)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarComponent(
    title: String,
    onMenuClick: () -> Unit
) {
    Column {
        CenterAlignedTopAppBar(
            title = {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    overflow = TextOverflow.Ellipsis
                )
            },
            navigationIcon = {
                IconButton(onClick = onMenuClick) {
                    Icon(Icons.Filled.Menu, contentDescription = "Menu")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
        )
        HorizontalDivider(
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
        )
    }
}

@Composable
fun NavigationDrawerComponent(
    userName: String,
    selectedScreen: String,
    onItemClick: (String) -> Unit,
    onLogout: () -> Unit
) {
    ModalDrawerSheet(
        modifier = Modifier.width(240.dp),
        drawerContainerColor = MaterialTheme.colorScheme.surface
    ) {
        // Usuario
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.user1),
                contentDescription = "User profile",
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = userName, style = MaterialTheme.typography.titleMedium)
        }

        HorizontalDivider()
        Spacer(modifier = Modifier.height(8.dp))

        // Opciones del menú
        val menuItems = listOf("Inicio", "Test", "Necesito ayuda", "Historial", "Cuenta")
        menuItems.forEach { item ->
            NavigationDrawerItem(
                modifier = Modifier
                    .padding(horizontal = 6.dp),
                icon = {
                    when (item) {
                        "Inicio" -> Icon(
                            painter = painterResource(id = R.drawable.ic_home),
                            contentDescription = null
                        )

                        "Test" -> Icon(
                            painter = painterResource(id = R.drawable.ic_test),
                            contentDescription = null
                        )

                        "Necesito ayuda" -> Icon(
                            painter = painterResource(id = R.drawable.ic_help),
                            contentDescription = null
                        )

                        "Historial" -> Icon(
                            painter = painterResource(id = R.drawable.ic_history),
                            contentDescription = null
                        )

                        "Cuenta" -> Icon(
                            painter = painterResource(id = R.drawable.ic_account),
                            contentDescription = null
                        )
                    }
                },
                label = { Text(item) },
                selected = selectedScreen == item,
                onClick = { onItemClick(item) }
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        NavigationDrawerItem(
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_logout),
                    contentDescription = null
                )
            },
            label = { Text("Cerrar sesión") },
            selected = false,
            onClick = onLogout
        )
    }
}

//@Composable
//fun ScreenContent(
//    selectedScreen: String,
//    paddingValues: PaddingValues,
//    onHelpMeClick: () -> Unit
//) {
//    // Mostrar el contenido correspondiente según la pantalla seleccionada
//    when (selectedScreen) {
//        "Inicio" -> HomeScreen(paddingValues, onHelpMeClick)
//        "Test" -> HomeScreen(paddingValues, onHelpMeClick)
//        "Necesito ayuda" -> HelpMeScreen()
//        "Historial" -> HomeScreen(paddingValues, onHelpMeClick)
//        "Cuenta" -> HomeScreen(paddingValues, onHelpMeClick)
//    }
//}

@Preview
@Composable
fun AppScaffoldPreview() {
    SisvitaG2Theme(darkTheme = false) {
        AppScaffoldComponent(
            userName = "Linna Jimenez",
            onLogout = {},
            navController = NavHostController(LocalContext.current)
        )
    }
}