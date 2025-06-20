package com.example.sisvitag2.ui.screens.account

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit // Para el botón de editar (futuro)
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController // <-- IMPORTAR NavController
import androidx.navigation.compose.rememberNavController // Para el Preview
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.sisvitag2.R
import com.example.sisvitag2.data.model.UserProfileData
import com.example.sisvitag2.ui.theme.SisvitaG2Theme
import com.example.sisvitag2.ui.vm.AccountUiState
import com.example.sisvitag2.ui.vm.AccountViewModel
import org.koin.androidx.compose.koinViewModel
import android.util.Log

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    navController: NavController, // <--- AÑADIDO para navegar a EditProfileScreen
    viewModel: AccountViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        if (uiState is AccountUiState.Idle) {
            Log.d("AccountScreen", "Estado Idle, llamando a loadUserProfile.")
            viewModel.loadUserProfile()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Perfil") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (val state = uiState) {
                is AccountUiState.LoadingProfile, AccountUiState.Idle -> { // Agrupar Loading e Idle para mostrar loader
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is AccountUiState.ProfileLoaded -> { // Cambiado de Success a ProfileLoaded
                    UserProfileContent(
                        userProfile = state.userProfile,
                        navController = navController // <--- Pasar NavController
                    )
                }
                is AccountUiState.UpdateSuccess -> { // Si hubo una actualización y se recargó el perfil
                    state.updatedProfile?.let {
                        UserProfileContent(userProfile = it, navController = navController)
                    } ?: CircularProgressIndicator(modifier = Modifier.align(Alignment.Center)) // Mostrar carga si el perfil actualizado es nulo
                }
                is AccountUiState.Error -> {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "Error: ${state.message}", color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadUserProfile() }) {
                            Text("Reintentar Cargar Perfil")
                        }
                    }
                }
                else -> { // Para AccountUiState.UpdatingProfile u otros futuros
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
            }
        }
    }
}

@Composable
fun UserProfileContent(
    userProfile: UserProfileData,
    navController: NavController // <--- AÑADIDO para el botón de editar
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(userProfile.photoUrl ?: R.drawable.user1)
                .crossfade(true)
                .error(R.drawable.user1)
                .placeholder(R.drawable.user1)
                .build(),
            contentDescription = "Foto de perfil",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
        )
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = userProfile.displayName ?: "Nombre no disponible",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        ProfileInfoCard {
            ProfileInfoRow(label = "Correo", value = userProfile.email)
            ProfileInfoRow(label = "Nombre(s)", value = userProfile.nombre)
            ProfileInfoRow(label = "A. Paterno", value = userProfile.apellidoPaterno)
            ProfileInfoRow(label = "A. Materno", value = userProfile.apellidoMaterno)
            ProfileInfoRow(label = "Fec. Nacimiento", value = userProfile.fechaNacimiento)
            ProfileInfoRow(label = "Tipo Documento", value = userProfile.tipoDocumento)
            ProfileInfoRow(label = "Nº Documento", value = userProfile.numeroDocumento)
            ProfileInfoRow(label = "Género", value = userProfile.genero)
            ProfileInfoRow(label = "Teléfono", value = userProfile.telefono)
            ProfileInfoRow(label = "Departamento", value = userProfile.departamento)
            ProfileInfoRow(label = "Provincia", value = userProfile.provincia)
            ProfileInfoRow(label = "Distrito", value = userProfile.distrito)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { navController.navigate("EditProfileRoute") }, // <--- NAVEGAR A EDITAR
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Editar Datos Personales")
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(
            onClick = { /* TODO: Implementar lógica de cambio de contraseña */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cambiar Contraseña")
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

// ProfileInfoCard y ProfileInfoRow (sin cambios)
@Composable
fun ProfileInfoCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

@Composable
fun ProfileInfoRow(label: String, value: String?) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value?.takeIf { it.isNotBlank() } ?: "No especificado",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (label != "Distrito") {
            Divider(modifier = Modifier.padding(top = 8.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AccountScreenContentPreview() {
    SisvitaG2Theme {
        val previewProfile = UserProfileData(
            uid = "preview_uid", email = "usuario@ejemplo.com", displayName = "Usuario de Muestra",
            photoUrl = null, nombre = "Muestra", apellidoPaterno = "Usuario",
            apellidoMaterno = "De Preview", fechaNacimiento = "15/08/1992", tipoDocumento = "DNI",
            numeroDocumento = "12345678", genero = "Masculino", telefono = "999888777",
            departamento = "Lima", provincia = "Lima", distrito = "Miraflores"
        )
        Surface(color = MaterialTheme.colorScheme.background) {
            // Para el preview, el NavController puede ser uno de prueba.
            UserProfileContent(userProfile = previewProfile, navController = rememberNavController())
        }
    }
}