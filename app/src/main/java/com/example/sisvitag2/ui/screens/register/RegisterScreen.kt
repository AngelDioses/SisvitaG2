package com.example.sisvitag2.ui.screens.register

import android.util.Log // Asegúrate que este import esté
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.sisvitag2.ui.theme.SisvitaG2Theme
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import androidx.navigation.NavGraph.Companion.findStartDestination // Para popUpTo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    navController: NavController,
    viewModel: RegisterViewModel = koinViewModel()
) {
    val registerUiState by viewModel.registerUiState.collectAsState()
    val dropdownsState by viewModel.dropdownDataState.collectAsState()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var middleName by remember { mutableStateOf("") }
    var documentType by remember { mutableStateOf("") }
    var documentCharacter by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("") }
    var province by remember { mutableStateOf("") }
    var district by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("") } // String YYYY-MM-DD

    var isDocumentTypeExpanded by remember { mutableStateOf(false) }
    var isGenderExpanded by remember { mutableStateOf(false) }
    var isDepartmentExpanded by remember { mutableStateOf(false) }
    var isProvinceExpanded by remember { mutableStateOf(false) }
    var isDistrictExpanded by remember { mutableStateOf(false) }

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

    val isRegistering = registerUiState is RegisterUiState.Registering

    LaunchedEffect(registerUiState) {
        when (val state = registerUiState) {
            is RegisterUiState.Success -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                navController.navigate("LoginRoute") { // <- CORREGIDO: Usar string de ruta
                    popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                    launchSingleTop = true
                }
                viewModel.resetRegisterState()
            }
            is RegisterUiState.Error -> {
                Toast.makeText(context, state.message ?: "Error desconocido", Toast.LENGTH_LONG).show()
                viewModel.resetRegisterState()
            }
            else -> { /* No-op */ }
        }
    }

    if (showDatePickerDialog) {
        DatePickerDialog(
            onDismissRequest = { showDatePickerDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    showDatePickerDialog = false
                    datePickerState.selectedDateMillis?.let { millis ->
                        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        birthDate = sdf.format(millis)
                    }
                }) { Text("Aceptar") }
            },
            dismissButton = { TextButton(onClick = { showDatePickerDialog = false }) { Text("Cancelar") } }
        ) { DatePicker(state = datePickerState) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Crear Cuenta", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(bottom = 16.dp).align(Alignment.CenterHorizontally))

        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Correo *") }, enabled = !isRegistering, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next))
        OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Contraseña *") }, visualTransformation = PasswordVisualTransformation(), enabled = !isRegistering, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next))
        OutlinedTextField(value = firstName, onValueChange = { firstName = it }, label = { Text("Nombre(s) *") }, enabled = !isRegistering, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = lastName, onValueChange = { lastName = it }, label = { Text("A. Paterno *") }, enabled = !isRegistering, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next))
            OutlinedTextField(value = middleName, onValueChange = { middleName = it }, label = { Text("A. Materno") }, enabled = !isRegistering, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DropdownInput(label = "Tipo Doc. *", selectedValue = documentType, options = dropdownsState.documentTypes, isLoading = dropdownsState.isLoadingDocumentTypes, expanded = isDocumentTypeExpanded, onExpandedChange = { isDocumentTypeExpanded = it }, onValueChange = { documentType = it; isDocumentTypeExpanded = false }, modifier = Modifier.weight(1f), enabled = !isRegistering)
            OutlinedTextField(value = documentCharacter, onValueChange = { if (it.all(Char::isDigit)) documentCharacter = it }, label = { Text("Nº Doc. *") }, enabled = !isRegistering, modifier = Modifier.weight(1.5f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DropdownInput(label = "Género *", selectedValue = gender, options = dropdownsState.genders, isLoading = dropdownsState.isLoadingGenders, expanded = isGenderExpanded, onExpandedChange = { isGenderExpanded = it }, onValueChange = { gender = it; isGenderExpanded = false }, modifier = Modifier.weight(1f), enabled = !isRegistering)
            OutlinedTextField(value = phone, onValueChange = { if (it.length <= 9 && it.all(Char::isDigit)) phone = it }, label = { Text("Teléfono") }, enabled = !isRegistering, modifier = Modifier.weight(1.5f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next))
        }
        OutlinedTextField(
            value = birthDate,
            onValueChange = { /* No editable */ },
            readOnly = true,
            label = { Text("Fec. Nacimiento *") },
            placeholder = { Text("YYYY-MM-DD") },
            trailingIcon = {
                IconButton(onClick = { showDatePickerDialog = true }, enabled = !isRegistering) {
                    Icon(
                        imageVector = Icons.Filled.CalendarToday, // <--- Usar imageVector
                        contentDescription = "Seleccionar fecha"
                    )                }
            },
            enabled = !isRegistering,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = if(isRegistering) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f) else MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = if(isRegistering) MaterialTheme.colorScheme.outline.copy(alpha = 0.38f) else MaterialTheme.colorScheme.outline,
                disabledPlaceholderColor = if(isRegistering) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f) else MaterialTheme.colorScheme.onSurfaceVariant,
                disabledLabelColor = if(isRegistering) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f) else MaterialTheme.colorScheme.onSurfaceVariant,
                disabledTrailingIconColor = if(isRegistering) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f) else MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        DropdownInput(label = "Departamento *", selectedValue = department, options = dropdownsState.departments, isLoading = dropdownsState.isLoadingDepartments, expanded = isDepartmentExpanded, onExpandedChange = { isDepartmentExpanded = it }, onValueChange = { department = it; isDepartmentExpanded = false; province = ""; district = ""; viewModel.getProvinces(it) }, modifier = Modifier.fillMaxWidth(), enabled = !isRegistering)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DropdownInput(label = "Provincia *", selectedValue = province, options = dropdownsState.provinces, isLoading = dropdownsState.isLoadingProvinces, expanded = isProvinceExpanded, onExpandedChange = { isProvinceExpanded = it }, onValueChange = { province = it; isProvinceExpanded = false; district = ""; viewModel.getDistricts(department, it) }, modifier = Modifier.weight(1f), enabled = !isRegistering && department.isNotBlank() && !dropdownsState.isLoadingProvinces)
            DropdownInput(label = "Distrito *", selectedValue = district, options = dropdownsState.districts, isLoading = dropdownsState.isLoadingDistricts, expanded = isDistrictExpanded, onExpandedChange = { isDistrictExpanded = it }, onValueChange = { district = it; isDistrictExpanded = false }, modifier = Modifier.weight(1f), enabled = !isRegistering && province.isNotBlank() && !dropdownsState.isLoadingDistricts)
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                focusManager.clearFocus()
                if (!isRegistering) {
                    val formDataForViewModel = mutableMapOf<String, Any>() // CORREGIDO: Iniciar como Map<String, Any>
                    formDataForViewModel["email_reg"] = email.trim()
                    formDataForViewModel["password_reg"] = password
                    formDataForViewModel["nombre"] = firstName.trim()
                    formDataForViewModel["apellidopaterno"] = lastName.trim()
                    middleName.trim().takeIf { it.isNotEmpty() }?.let { formDataForViewModel["apellidomaterno"] = it }
                    formDataForViewModel["tipo_documento"] = documentType
                    formDataForViewModel["numero_documento"] = documentCharacter.trim()
                    formDataForViewModel["genero"] = gender
                    phone.trim().takeIf { it.isNotEmpty() }?.let { formDataForViewModel["telefono"] = it }
                    formDataForViewModel["departamento"] = department
                    formDataForViewModel["provincia"] = province
                    formDataForViewModel["distrito"] = district
                    formDataForViewModel["fechanacimiento_str"] = birthDate
                    formDataForViewModel["role_description"] = "Paciente"

                    viewModel.performRegistration(formDataForViewModel.toMap()) // <- CORREGIDO: Pasar Map<String, Any>
                }
            },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            enabled = !isRegistering
        ) {
            if (isRegistering) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
            } else {
                Text("Crear Cuenta", fontSize = 16.sp)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "¿Ya tienes una cuenta? Inicia Sesión",
            color = MaterialTheme.colorScheme.primary,
            fontSize = 14.sp,
            modifier = Modifier
                .clickable(enabled = !isRegistering) {
                    navController.navigate("LoginRoute") { // <- CORREGIDO: Usar string de ruta
                        popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                    }
                }
                .padding(8.dp)
                .align(Alignment.CenterHorizontally)
        )
    }
}

// DropdownInput (sin cambios respecto a la última versión que te di, que ya manejaba options.isNotEmpty())
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownInput(
    label: String, selectedValue: String, options: List<String>, isLoading: Boolean,
    expanded: Boolean, onExpandedChange: (Boolean) -> Unit, onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier, enabled: Boolean = true
) {
    ExposedDropdownMenuBox(
        expanded = expanded && !isLoading && enabled && options.isNotEmpty(),
        onExpandedChange = { if (!isLoading && enabled && options.isNotEmpty()) onExpandedChange(!expanded) },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedValue,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                if (isLoading) {
                    CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded && enabled && options.isNotEmpty())
                }
            },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            enabled = enabled,
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
        )
        ExposedDropdownMenu(
            expanded = expanded && !isLoading && enabled && options.isNotEmpty(),
            onDismissRequest = { onExpandedChange(false) }
        ) {
            if (options.isEmpty() && !isLoading) { // Mostrar si no hay opciones y no está cargando
                DropdownMenuItem(
                    text = { Text("No hay opciones") },
                    onClick = { },
                    enabled = false
                )
            } else {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = { onValueChange(option); onExpandedChange(false) }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
fun RegisterScreenPreview() {
    SisvitaG2Theme {
        RegisterScreen(navController = rememberNavController())
    }
}