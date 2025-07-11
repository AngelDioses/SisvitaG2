package com.example.sisvitag2.ui.screens.account

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.sisvitag2.R
import com.example.sisvitag2.ui.vm.AccountViewModel
import com.example.sisvitag2.ui.vm.AccountUiState
import com.example.sisvitag2.ui.vm.SessionViewModel
import org.koin.androidx.compose.koinViewModel
import android.util.Log
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    navController: NavController,
    viewModel: AccountViewModel = koinViewModel(),
    sessionViewModel: SessionViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    
    // Conectar el callback y la referencia directa para actualizar SessionViewModel
    LaunchedEffect(Unit) {
        viewModel.onProfileUpdated = {
            Log.d("EditProfileScreen", "Callback onProfileUpdated recibido, actualizando SessionViewModel...")
            // Forzar actualización del SessionViewModel
            sessionViewModel.fetchUserNameData(sessionViewModel.auth.currentUser)
            Log.d("EditProfileScreen", "SessionViewModel actualizado.")
        }
        
        // SessionViewModel ya está inyectado en AccountViewModel, no necesitamos conectarlo manualmente
        Log.d("EditProfileScreen", "AccountViewModel ya tiene SessionViewModel inyectado.")
    }

    var nombre by remember { mutableStateOf("") }
    var apellidoPaterno by remember { mutableStateOf("") }
    var apellidoMaterno by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var departamento by remember { mutableStateOf("") }
    var provincia by remember { mutableStateOf("") }
    var distrito by remember { mutableStateOf("") }
    var fechaNacimiento by remember { mutableStateOf("") }
    
    var showDatePickerDialog by remember { mutableStateOf(false) }
    val maxDateCalendar = Calendar.getInstance().apply { add(Calendar.YEAR, -18) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = maxDateCalendar.timeInMillis,
        yearRange = (Calendar.getInstance().get(Calendar.YEAR) - 100)..(maxDateCalendar.get(Calendar.YEAR)),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return utcTimeMillis <= maxDateCalendar.timeInMillis
            }
        }
    )
    
    // Launcher para seleccionar imagen
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.updateUserProfilePicture(it) }
    }

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
                        telefono != (profile.telefono ?: "") ||
                        departamento != (profile.departamento ?: "") ||
                        provincia != (profile.provincia ?: "") ||
                        distrito != (profile.distrito ?: "") ||
                        fechaNacimiento != (profile.fechaNacimiento ?: ""))
            ) {
                Log.d("EditProfileScreen", "Inicializando campos desde perfil: ${profile.displayName}")
                nombre = profile.nombre ?: ""
                apellidoPaterno = profile.apellidoPaterno ?: ""
                apellidoMaterno = profile.apellidoMaterno ?: ""
                telefono = profile.telefono ?: ""
                departamento = profile.departamento ?: ""
                provincia = profile.provincia ?: ""
                distrito = profile.distrito ?: ""
                fechaNacimiento = profile.fechaNacimiento?.toDate()?.let { java.text.SimpleDateFormat("yyyy-MM-dd").format(it) } ?: ""
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
    
    // DatePicker Dialog
    if (showDatePickerDialog) {
        DatePickerDialog(
            onDismissRequest = { showDatePickerDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    showDatePickerDialog = false
                    datePickerState.selectedDateMillis?.let { millis ->
                        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        fechaNacimiento = sdf.format(millis)
                    }
                }) { Text("Aceptar") }
            },
            dismissButton = { TextButton(onClick = { showDatePickerDialog = false }) { Text("Cancelar") } }
        ) { DatePicker(state = datePickerState) }
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
                    // Foto de perfil
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                            .clickable(enabled = !isLoading) { imagePickerLauncher.launch("image/*") }
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(currentProfile?.photoUrl ?: R.drawable.user1)
                                .crossfade(true)
                                .error(R.drawable.user1)
                                .placeholder(R.drawable.user1)
                                .build(),
                            contentDescription = "Foto de perfil",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        
                        // Icono de agregar foto
                        Icon(
                            imageVector = Icons.Filled.AddAPhoto,
                            contentDescription = "Cambiar foto",
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .background(
                                    MaterialTheme.colorScheme.primary,
                                    CircleShape
                                )
                                .padding(8.dp)
                                .size(24.dp),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    
                    Text(
                        text = "Toca la imagen para cambiar la foto de perfil",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Campos de texto
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        label = { Text("Nombre(s) *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Next
                        ),
                        enabled = !isLoading
                    )
                    
                    OutlinedTextField(
                        value = apellidoPaterno,
                        onValueChange = { apellidoPaterno = it },
                        label = { Text("Apellido Paterno *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Next
                        ),
                        enabled = !isLoading
                    )
                    
                    OutlinedTextField(
                        value = apellidoMaterno,
                        onValueChange = { apellidoMaterno = it },
                        label = { Text("Apellido Materno") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Next
                        ),
                        enabled = !isLoading
                    )
                    
                    OutlinedTextField(
                        value = telefono,
                        onValueChange = { if (it.length <= 9 && it.all(Char::isDigit)) telefono = it },
                        label = { Text("Teléfono") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Phone,
                            imeAction = ImeAction.Next
                        ),
                        singleLine = true,
                        enabled = !isLoading
                    )
                    
                    // Fecha de nacimiento
                    OutlinedTextField(
                        value = fechaNacimiento,
                        onValueChange = { /* No editable */ },
                        readOnly = true,
                        label = { Text("Fecha de Nacimiento") },
                        placeholder = { Text("YYYY-MM-DD") },
                        trailingIcon = {
                            IconButton(onClick = { showDatePickerDialog = true }, enabled = !isLoading) {
                                Icon(
                                    imageVector = Icons.Filled.CalendarToday,
                                    contentDescription = "Seleccionar fecha"
                                )
                            }
                        },
                        enabled = !isLoading,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                            disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f),
                            disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.60f),
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.60f),
                            disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                        )
                    )
                    
                    // Campos de ubicación (solo lectura por ahora)
                    OutlinedTextField(
                        value = departamento,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Departamento") },
                        enabled = false,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                            disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f),
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.60f)
                        )
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = provincia,
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Provincia") },
                            enabled = false,
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                                disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f),
                                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.60f)
                            )
                        )
                        
                        OutlinedTextField(
                            value = distrito,
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Distrito") },
                            enabled = false,
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                                disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f),
                                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.60f)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            val originalProfile = currentProfile
                            // Construir el mapa con TODOS los campos requeridos
                            val dataToUpdate = mutableMapOf<String, Any?>()
                            dataToUpdate["nombre"] = nombre.trim()
                            dataToUpdate["apellidopaterno"] = apellidoPaterno.trim()
                            dataToUpdate["apellidomaterno"] = apellidoMaterno.trim()
                            dataToUpdate["telefono"] = telefono.trim()
                            dataToUpdate["departamento"] = departamento
                            dataToUpdate["provincia"] = provincia
                            dataToUpdate["distrito"] = distrito
                            // Campos requeridos por reglas Firestore
                            dataToUpdate["uid"] = originalProfile?.uid ?: ""
                            dataToUpdate["correo"] = originalProfile?.email ?: ""
                            dataToUpdate["tipousuarioid"] = originalProfile?.tipousuarioid ?: ""
                            dataToUpdate["legacyTipoUsuarioId"] = originalProfile?.legacyTipoUsuarioId ?: 1
                            dataToUpdate["ubigeoid"] = originalProfile?.ubigeoid ?: ""
                            dataToUpdate["tipo_documento"] = originalProfile?.tipoDocumento ?: ""
                            dataToUpdate["numero_documento"] = originalProfile?.numeroDocumento ?: ""
                            dataToUpdate["genero"] = originalProfile?.genero ?: ""
                            dataToUpdate["estado"] = originalProfile?.estado ?: "aprobado"
                            // Fecha de nacimiento
                            if (fechaNacimiento.isNotBlank() && fechaNacimiento != originalProfile?.fechaNacimiento?.toDate()?.let { java.text.SimpleDateFormat("yyyy-MM-dd").format(it) }) {
                                // El usuario cambió la fecha, convertir a Timestamp
                                try {
                                    val parts = fechaNacimiento.split("-")
                                    if (parts.size == 3) {
                                        val year = parts[0].toInt()
                                        val month = parts[1].toInt() - 1 // Mes base 0
                                        val day = parts[2].toInt()
                                        val cal = java.util.Calendar.getInstance()
                                        cal.set(year, month, day, 0, 0, 0)
                                        dataToUpdate["fechanacimiento"] = com.google.firebase.Timestamp(cal.time)
                                    }
                                } catch (e: Exception) {
                                    // Si falla, usa el valor original
                                    dataToUpdate["fechanacimiento"] = originalProfile?.fechaNacimiento ?: ""
                                }
                            } else if (originalProfile?.fechaNacimiento != null) {
                                dataToUpdate["fechanacimiento"] = originalProfile.fechaNacimiento
                            }
                            Log.d("EditProfileScreen", "Datos enviados a Firestore para update: " + dataToUpdate.map { it.key + ": " + it.value?.toString() })
                            viewModel.updateUserPersonalData(dataToUpdate)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text("Guardar Cambios")
                        }
                    }
                }
            }
        }
    }
}