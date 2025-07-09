package com.example.sisvitag2.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.LayoutDirection // Para calculateStartPadding/calculateEndPadding
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController // Asegúrate de este import para el Preview
import coil.compose.AsyncImage // Si usas AsyncImage aquí también
import coil.request.ImageRequest // Si usas AsyncImage aquí también
import com.example.sisvitag2.R
import com.example.sisvitag2.ui.navigation.AppNavHost
import com.example.sisvitag2.ui.theme.SisvitaG2Theme
import com.example.sisvitag2.ui.vm.SessionViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import android.util.Log
import org.koin.androidx.compose.get

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffoldComponent(
    onLogout: () -> Unit,
    navController: NavHostController
) {
    val sessionViewModel: SessionViewModel = koinViewModel()
    val userName by sessionViewModel.userName
    val userPhotoUrl by sessionViewModel.userPhotoUrl
    val userRol by sessionViewModel.userRol
    val userEstado by sessionViewModel.userEstado
    
    // Log para verificar si userName se actualiza
    LaunchedEffect(userName) {
        Log.d("AppScaffoldComponent", "userName actualizado en AppScaffold: $userName")
    }
    
    // Log para verificar si userEstado se actualiza
    LaunchedEffect(userEstado) {
        Log.d("AppScaffoldComponent", "userEstado actualizado en AppScaffold: $userEstado")
    }
    
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var selectedScreen by remember { mutableStateOf("Inicio") }

    LaunchedEffect(navController) {
        navController.currentBackStackEntryFlow.collect { backStackEntry ->
            selectedScreen = when (backStackEntry.destination.route) {
                "Inicio" -> "Inicio"
                "Test" -> "Test"
                "Necesito ayuda" -> "Necesito Ayuda"
                "Historial" -> "Historial"
                "Cuenta" -> "Cuenta"
                else -> backStackEntry.destination.route?.takeIf { it.startsWith("DoTest") }?.let { "Test" } ?: "Inicio"
            }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            NavigationDrawerComponent(
                userName = userName ?: "Usuario",
                userPhotoUrl = userPhotoUrl,
                selectedScreen = selectedScreen,
                userRol = userRol,
                onItemClick = { screenRoute ->
                    selectedScreen = screenRoute // O el título amigable correspondiente
                    scope.launch { drawerState.close() }
                    navController.navigate(screenRoute) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
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
                    title = if (selectedScreen == "Inicio" && navController.currentDestination?.route == "Inicio") "Sisvita" else selectedScreen,
                    onMenuClick = {
                        scope.launch { drawerState.open() }
                    }
                )
            },
            content = { paddingValues ->
                AppNavHost(
                    navController = navController,
                    paddingValues = paddingValues
                )
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
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1
                )
            },
            navigationIcon = {
                IconButton(onClick = onMenuClick) {
                    Icon(Icons.Filled.Menu, contentDescription = "Menu")
                }
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
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
    userPhotoUrl: String?,
    selectedScreen: String,
    userRol: Int?,
    onItemClick: (String) -> Unit,
    onLogout: () -> Unit
) {
    ModalDrawerSheet(
        modifier = Modifier.width(280.dp),
        drawerContainerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Cargar la foto del usuario desde URL o usar placeholder
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(userPhotoUrl ?: R.drawable.user1)
                    .crossfade(true)
                    .error(R.drawable.user1)
                    .placeholder(R.drawable.user1)
                    .build(),
                contentDescription = "Foto de perfil",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .border(BorderStroke(2.dp, MaterialTheme.colorScheme.primary), CircleShape)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = userName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

        val menuItems = buildList {
            add("Inicio" to R.drawable.ic_home)
            if (userRol == 1) add("Test" to R.drawable.ic_test) // Solo Persona
            if (userRol == 2) add("Bandeja" to R.drawable.ic_history) // Solo Especialista
            if (userRol == 1) add("Necesito ayuda" to R.drawable.ic_help) // Solo Persona
            add("Cuenta" to R.drawable.ic_account)
        }

        menuItems.forEach { (route, iconRes) ->
            val itemPadding = NavigationDrawerItemDefaults.ItemPadding // Guardar para usar
            NavigationDrawerItem(
                modifier = Modifier.padding( // Usar la alternativa si .copy da error
                    start = itemPadding.calculateStartPadding(LayoutDirection.Ltr),
                    top = itemPadding.calculateTopPadding(),
                    end = itemPadding.calculateEndPadding(LayoutDirection.Ltr),
                    bottom = itemPadding.calculateBottomPadding()
                ).padding(horizontal = 12.dp - itemPadding.calculateStartPadding(LayoutDirection.Ltr)), // Ajuste para que el padding total sea ~12dp horizontal
                icon = { Icon(painter = painterResource(id = iconRes), contentDescription = route) },
                label = { Text(route) },
                selected = selectedScreen == route, // Compara con la ruta
                onClick = { onItemClick(route) }
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        val itemPadding = NavigationDrawerItemDefaults.ItemPadding
        NavigationDrawerItem(
            modifier = Modifier.padding(
                start = itemPadding.calculateStartPadding(LayoutDirection.Ltr),
                top = itemPadding.calculateTopPadding(),
                end = itemPadding.calculateEndPadding(LayoutDirection.Ltr),
                bottom = itemPadding.calculateBottomPadding()
            ).padding(horizontal = 12.dp - itemPadding.calculateStartPadding(LayoutDirection.Ltr)),
            icon = { Icon(painter = painterResource(id = R.drawable.ic_logout), contentDescription = "Cerrar sesión") },
            label = { Text("Cerrar sesión") },
            selected = false,
            onClick = onLogout
        )
        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun AppScaffoldPreview() {
    SisvitaG2Theme(darkTheme = false) {
        val previewNavController = rememberNavController() // <- Esto debería funcionar si navigation-compose está bien.
        AppScaffoldComponent(
            onLogout = {},
            navController = previewNavController
        )
    }
}