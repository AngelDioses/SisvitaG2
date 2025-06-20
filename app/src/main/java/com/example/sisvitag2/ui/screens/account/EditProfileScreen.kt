package com.example.sisvitag2.ui.screens.account

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.sisvitag2.ui.vm.AccountViewModel
import com.example.sisvitag2.ui.vm.AccountUiState
import org.koin.androidx.compose.koinViewModel
import android.util.Log

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    navController: NavController,
    viewModel: AccountViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    var nombre by remember { mutableStateOf("") }
    var apellidoPaterno by remember { mutableStateOf("") }
    var apellidoMaterno by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    // ... otros campos si los haces editables ...

    var fieldsInitialized by remember { mutableStateOf(false) }
    val currentProfile = (uiState as? AccountUiState.ProfileLoaded)?.userProfile ?:
    (uiState as? AccountUiState.UpdateSuccess)?.updatedProfile

    // Inicializar campos cuando el perfil se carga o actualiza exitosamente
    LaunchedEffect(currentProfile) {
        currentProfile?.let { profile ->
            if (!fieldsInitialized || // Inicializar la primera vez
                (nombre != profile.nombre || // O si los datos del VM cambiaron
                        apellidoPaterno != profile.apellidoPaterno ||
                        apellidoMaterno != (profile.apellidoMaterno ?: "") ||
                        telefono != (profile.telefono ?: ""))
            ) {
                Log.d("EditProfileScreen", "Inicializando campos desde perfil: ${profile.displayName}")
                nombre = profile.nombre ?: ""
                apellidoPaterno = profile.apellidoPaterno ?: ""
                apellidoMaterno = profile.apellidoMaterno ?: ""
                telefono = profile.telefono ?: ""
                fieldsInitialized = true
            }
        }
    }

    // Manejar mensajes de éxito/error de la actualización
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is AccountUiState.UpdateSuccess -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                fieldsInitialized = false // Permitir reinicialización si es necesario
                viewModel.acknowledgeUpdate() // Volver a ProfileLoaded o recargar
                navController.popBackStack() // Volver a la pantalla de perfil
            }
            is AccountUiState.Error -> {
                // No mostrar Toast si el error ya fue por carga inicial y no por actualización
                if (state.message != "No se pudo cargar el perfil." && state.message != "Usuario no autenticado.") {
                    Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                }
                // El estado Error ya es manejado por el `when` principal para mostrar el mensaje
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar Mi Perfil") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        when (uiState) {
            is AccountUiState.LoadingProfile, AccountUiState.Idle -> {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is AccountUiState.Error -> {
                // Si el perfil no se pudo cargar inicialmente.
                if (!fieldsInitialized) { // Solo si los campos no se pudieron inicializar
                    Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                        Text("Error al cargar datos del perfil: ${(uiState as AccountUiState.Error).message}", color = MaterialTheme.colorScheme.error)
                    }
                }
                // Si el error fue de actualización, el Toast ya se mostró y el form sigue visible
            }
            is AccountUiState.ProfileLoaded, is AccountUiState.UpdatingProfile, is AccountUiState.UpdateSuccess -> {
                // Muestra el formulario si el perfil está cargado, actualizándose o si la actualización fue exitosa (antes de resetear el estado)
                val isLoading = uiState is AccountUiState.UpdatingProfile

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        label = { Text("Nombre(s) *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !isLoading
                    )
                    OutlinedTextField(
                        value = apellidoPaterno,
                        onValueChange = { apellidoPaterno = it },
                        label = { Text("Apellido Paterno *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !isLoading
                    )
                    OutlinedTextField(
                        value = apellidoMaterno,
                        onValueChange = { apellidoMaterno = it },
                        label = { Text("Apellido Materno") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !isLoading
                    )
                    OutlinedTextField(
                        value = telefono,
                        onValueChange = { if (it.length <= 9 && it.all(Char::isDigit)) telefono = it },
                        label = { Text("Teléfono") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        enabled = !isLoading
                    )

                    // Aquí irían los campos más complejos como Fecha de Nacimiento, Ubicación, etc.
                    // si decides hacerlos editables. Requerirían DatePickers y Dropdowns.

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            val originalProfile = (currentProfile) // El perfil cargado antes de la edición

                            val updatedData = mutableMapOf<String, Any>()
                            if (nombre != originalProfile?.nombre) updatedData["nombre"] = nombre.trim()
                            if (apellidoPaterno != originalProfile?.apellidoPaterno) updatedData["apellidopaterno"] = apellidoPaterno.trim()

                            // Para campos opcionales, enviar solo si realmente cambiaron o si se pueden blanquear
                            if (apellidoMaterno != (originalProfile?.apellidoMaterno ?: "")) updatedData["apellidomaterno"] = apellidoMaterno.trim()
                            if (telefono != (originalProfile?.telefono ?: "")) updatedData["telefono"] = telefono.trim()

                            if (updatedData.isNotEmpty()) {
                                viewModel.updateUserPersonalData(updatedData)
                            } else {
                                Toast.makeText(context, "No hay cambios para guardar.", Toast.LENGTH_SHORT).show()
                                navController.popBackStack() // Vuelve si no hay cambios
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Guardar Cambios")
                        }
                    }
                }
            }
        }
    }
}