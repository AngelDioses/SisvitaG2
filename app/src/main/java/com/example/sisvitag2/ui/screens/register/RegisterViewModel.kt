package com.example.sisvitag2.ui.screens.register

import android.util.Log
import androidx.core.util.PatternsCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sisvitag2.data.repository.RegisterRepository
import com.example.sisvitag2.data.repository.RegisterResult
import com.example.sisvitag2.data.repository.RegisterError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

sealed class RegisterUiState {
    object Idle : RegisterUiState()
    object LoadingInitialData : RegisterUiState()
    object Registering : RegisterUiState()
    data class Success(val message: String) : RegisterUiState()
    data class Error(val errorType: RegisterError, val message: String?) : RegisterUiState()
}

data class DropdownDataState(
    val documentTypes: List<String> = emptyList(),
    val genders: List<String> = emptyList(),
    val departments: List<String> = emptyList(),
    val provinces: List<String> = emptyList(),
    val districts: List<String> = emptyList(),
    val isLoadingDocumentTypes: Boolean = false,
    val isLoadingGenders: Boolean = false,
    val isLoadingDepartments: Boolean = false,
    val isLoadingProvinces: Boolean = false,
    val isLoadingDistricts: Boolean = false
)

class RegisterViewModel(
    private val registerRepository: RegisterRepository
) : ViewModel() {
    
    // Función para notificar cambios de autenticación
    var onAuthStateChanged: (() -> Unit)? = null

    private val _registerUiState = MutableStateFlow<RegisterUiState>(RegisterUiState.Idle)
    val registerUiState: StateFlow<RegisterUiState> = _registerUiState.asStateFlow()

    private val _dropdownDataState = MutableStateFlow(DropdownDataState())
    val dropdownDataState: StateFlow<DropdownDataState> = _dropdownDataState.asStateFlow()

    // Scope independiente que no se cancela con el ViewModel
    private val independentScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object { private const val TAG = "RegisterViewModel" }

    init { loadInitialDropdownData() }

    private fun loadInitialDropdownData() {
        if ((_dropdownDataState.value.documentTypes.isNotEmpty() &&
                    _dropdownDataState.value.genders.isNotEmpty() &&
                    _dropdownDataState.value.departments.isNotEmpty()) ||
            _registerUiState.value == RegisterUiState.LoadingInitialData) return

        _registerUiState.value = RegisterUiState.LoadingInitialData
        _dropdownDataState.update { it.copy(isLoadingDocumentTypes = true, isLoadingGenders = true, isLoadingDepartments = true) }

        viewModelScope.launch {
            var success = true
            try {
                Log.d(TAG, "loadInitialDropdownData: Cargando datos para dropdowns...")
                val docTypes = registerRepository.getDocumentTypes()
                val genders = registerRepository.getGenders()
                val departments = registerRepository.getDepartments()
                _dropdownDataState.update { it.copy(documentTypes = docTypes, genders = genders, departments = departments) }
                Log.d(TAG, "loadInitialDropdownData: Datos cargados. DocTypes: ${docTypes.size}, Genders: ${genders.size}, Departments: ${departments.size}")
            } catch (e: Exception) {
                Log.e(TAG, "loadInitialDropdownData: Error cargando datos iniciales", e)
                success = false
                _registerUiState.value = RegisterUiState.Error(RegisterError.UNKNOWN, "Error al cargar opciones iniciales.")
            } finally {
                _dropdownDataState.update { it.copy(isLoadingDocumentTypes = false, isLoadingGenders = false, isLoadingDepartments = false) }
                if (success && _registerUiState.value == RegisterUiState.LoadingInitialData) {
                    _registerUiState.value = RegisterUiState.Idle
                }
            }
        }
    }

    fun getProvinces(department: String) {
        if (department.isBlank() || _dropdownDataState.value.isLoadingProvinces) return
        viewModelScope.launch {
            _dropdownDataState.update { it.copy(isLoadingProvinces = true, provinces = emptyList(), districts = emptyList()) }
            try {
                _dropdownDataState.update { it.copy(provinces = registerRepository.getProvinces(department)) }
            } catch (e: Exception) {
                Log.e(TAG, "Error cargando provincias para $department", e)
            } finally {
                _dropdownDataState.update { it.copy(isLoadingProvinces = false) }
            }
        }
    }

    fun getDistricts(department: String, province: String) {
        if (department.isBlank() || province.isBlank() || _dropdownDataState.value.isLoadingDistricts) return
        viewModelScope.launch {
            _dropdownDataState.update { it.copy(isLoadingDistricts = true, districts = emptyList()) }
            try {
                _dropdownDataState.update { it.copy(districts = registerRepository.getDistricts(department, province)) }
            } catch (e: Exception) {
                Log.e(TAG, "Error cargando distritos para $department/$province", e)
            } finally {
                _dropdownDataState.update { it.copy(isLoadingDistricts = false) }
            }
        }
    }

    fun performRegistration(formData: Map<String, Any>) {
        if (_registerUiState.value is RegisterUiState.Registering) {
            Log.w(TAG, "Registro ya en progreso.")
            return
        }

        val email = formData["email_reg"] as? String ?: ""
        val password = formData["password_reg"] as? String ?: ""
        var errorMsg: String? = null
        when {
            email.isBlank() -> errorMsg = "El correo es obligatorio."
            !PatternsCompat.EMAIL_ADDRESS.matcher(email).matches() -> errorMsg = "Formato de correo inválido."
            password.isBlank() -> errorMsg = "La contraseña es obligatoria."
            password.length < 6 -> errorMsg = "La contraseña debe tener al menos 6 caracteres."
            (formData["nombre"] as? String).isNullOrBlank() -> errorMsg = "El nombre es obligatorio."
            (formData["apellidopaterno"] as? String).isNullOrBlank() -> errorMsg = "El apellido paterno es obligatorio."
            (formData["fechanacimiento_str"] as? String).isNullOrBlank() -> errorMsg = "La fecha de nacimiento es obligatoria."
            !(isValidBirthDateFormat(formData["fechanacimiento_str"] as? String ?: "")) -> errorMsg = "Formato de fecha (YYYY-MM-DD) incorrecto."
            !(isOldEnough(formData["fechanacimiento_str"] as? String ?: "")) -> errorMsg = "Debes ser mayor de 18 años."
            (formData["departamento"] as? String).isNullOrBlank() -> errorMsg = "El departamento es obligatorio."
            (formData["provincia"] as? String).isNullOrBlank() -> errorMsg = "La provincia es obligatoria."
            (formData["distrito"] as? String).isNullOrBlank() -> errorMsg = "El distrito es obligatorio."
            (formData["tipo_documento"] as? String).isNullOrBlank() -> errorMsg = "El tipo de documento es obligatorio."
            (formData["numero_documento"] as? String).isNullOrBlank() -> errorMsg = "El número de documento es obligatorio."
            (formData["genero"] as? String).isNullOrBlank() -> errorMsg = "El género es obligatorio."
        }
        if (errorMsg != null) {
            Log.w(TAG, "Validación fallida: $errorMsg")
            _registerUiState.value = RegisterUiState.Error(RegisterError.EMPTY_CREDENTIALS, errorMsg)
            return
        }

        // Aquí deberías asegurarte de que el formData contiene legacyTipoUsuarioId
        // Por ejemplo, si tienes un campo 'rol' en el formulario:
        // val legacyTipoUsuarioId = if (formData["rol"] == "Especialista") 2 else 1
        // y luego agregarlo al formData
        //
        // Si ya lo tienes, omite este bloque. Si no, puedes hacer:
        val legacyTipoUsuarioId = when (formData["rol"] as? String) {
            "Especialista" -> 2
            else -> 1 // Por defecto Persona
        }
        val formDataWithRole = formData.toMutableMap().apply { put("legacyTipoUsuarioId", legacyTipoUsuarioId) }

        _registerUiState.value = RegisterUiState.Registering
        Log.d(TAG, "performRegistration: ViewModel iniciando corutina de registro.")
        
        // Usar scope independiente para el registro
        independentScope.launch {
            try {
                // Pequeño delay para evitar cancelaciones prematuras
                delay(100)
                
                val result = registerRepository.register(email, password, formDataWithRole)
                Log.d(TAG, "performRegistration: Resultado del repositorio: $result")
                
                when (result) {
                    is RegisterResult.Success -> {
                        _registerUiState.value = RegisterUiState.Success("Usuario registrado. Revisa tu correo para verificar tu cuenta.")
                        // Notificar cambio de estado de autenticación
                        onAuthStateChanged?.invoke()
                    }
                    is RegisterResult.Failure -> {
                        _registerUiState.value = RegisterUiState.Error(result.errorType, result.message)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "performRegistration: Error inesperado", e)
                _registerUiState.value = RegisterUiState.Error(RegisterError.UNKNOWN, "Error inesperado durante el registro")
            }
        }
    }

    fun resetRegisterState() {
        if (_registerUiState.value is RegisterUiState.Error || _registerUiState.value is RegisterUiState.Success) {
            _registerUiState.value = RegisterUiState.Idle
            Log.d(TAG, "Estado de registro reseteado a Idle.")
        }
    }

    private fun isValidBirthDateFormat(dateStr: String): Boolean = dateStr.matches(Regex("^\\d{4}-\\d{2}-\\d{2}$"))
    private fun isOldEnough(dateStr: String): Boolean {
        if (!isValidBirthDateFormat(dateStr)) return false
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply { isLenient = false }
            val birthDate = sdf.parse(dateStr) ?: return false
            val eighteenYearsAgo = Calendar.getInstance().apply { add(Calendar.YEAR, -18); add(Calendar.DAY_OF_YEAR, 1) }
            !birthDate.after(eighteenYearsAgo.time)
        } catch (e: Exception) { false }
    }
}