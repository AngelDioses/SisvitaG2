package com.example.sisvitag2.ui.screens.register

import android.util.Log
import androidx.core.util.PatternsCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sisvitag2.data.repository.RegisterRepository
import com.example.sisvitag2.data.repository.RegisterResult
import com.example.sisvitag2.data.repository.RegisterError // Asegúrate que este enum esté en el mismo paquete o importado
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat // Para validación de fecha en ViewModel
import java.util.Calendar
import java.util.Locale

// Estados de la UI (RegisterUiState, DropdownDataState - como los tenías)
sealed class RegisterUiState {
    object Idle : RegisterUiState()
    object LoadingInitialData : RegisterUiState()
    object Registering : RegisterUiState()
    data class Success(val message: String = "¡Registro completado!") : RegisterUiState()
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

    private val _registerUiState = MutableStateFlow<RegisterUiState>(RegisterUiState.Idle)
    val registerUiState: StateFlow<RegisterUiState> = _registerUiState.asStateFlow()

    private val _dropdownDataState = MutableStateFlow(DropdownDataState())
    val dropdownDataState: StateFlow<DropdownDataState> = _dropdownDataState.asStateFlow()

    companion object {
        private const val TAG = "RegisterViewModel"
    }

    init {
        loadInitialDropdownData()
    }

    private fun loadInitialDropdownData() {
        if (_dropdownDataState.value.departments.isNotEmpty() &&
            _dropdownDataState.value.documentTypes.isNotEmpty() && // Chequear todos los relevantes
            _dropdownDataState.value.genders.isNotEmpty() ||
            _registerUiState.value == RegisterUiState.LoadingInitialData
        ) return

        _registerUiState.value = RegisterUiState.LoadingInitialData
        _dropdownDataState.update { it.copy(isLoadingDocumentTypes = true, isLoadingGenders = true, isLoadingDepartments = true) }

        viewModelScope.launch {
            var success = true
            try {
                val docTypes = registerRepository.getDocumentTypes()
                val genders = registerRepository.getGenders()
                val departments = registerRepository.getDepartments()
                _dropdownDataState.update { it.copy(documentTypes = docTypes, genders = genders, departments = departments) }
            } catch (e: Exception) {
                Log.e(TAG, "Error cargando datos iniciales", e)
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
        if (_registerUiState.value is RegisterUiState.Registering) return

        val email = formData["email_reg"] as? String ?: ""
        val password = formData["password_reg"] as? String ?: ""
        val nombre = formData["nombre"] as? String ?: ""
        val apellidoPaterno = formData["apellidopaterno"] as? String ?: ""
        val fechaNacimientoStr = formData["fechanacimiento_str"] as? String ?: ""
        val departamento = formData["departamento"] as? String ?: ""
        val provincia = formData["provincia"] as? String ?: ""
        val distrito = formData["distrito"] as? String ?: ""
        val tipoDocumento = formData["tipo_documento"] as? String ?: ""
        val numeroDocumento = formData["numero_documento"] as? String ?: ""
        val genero = formData["genero"] as? String ?: ""

        var errorMsg: String? = null
        when {
            email.isBlank() -> errorMsg = "El correo es obligatorio."
            !PatternsCompat.EMAIL_ADDRESS.matcher(email).matches() -> errorMsg = "Formato de correo inválido."
            password.isBlank() -> errorMsg = "La contraseña es obligatoria."
            password.length < 6 -> errorMsg = "La contraseña debe tener al menos 6 caracteres."
            nombre.isBlank() -> errorMsg = "El nombre es obligatorio."
            apellidoPaterno.isBlank() -> errorMsg = "El apellido paterno es obligatorio."
            fechaNacimientoStr.isBlank() -> errorMsg = "La fecha de nacimiento es obligatoria."
            !isValidBirthDateFormat(fechaNacimientoStr) -> errorMsg = "Formato de fecha (YYYY-MM-DD) incorrecto."
            !isOldEnough(fechaNacimientoStr) -> errorMsg = "Debes ser mayor de 18 años."
            departamento.isBlank() -> errorMsg = "El departamento es obligatorio."
            provincia.isBlank() -> errorMsg = "La provincia es obligatoria."
            distrito.isBlank() -> errorMsg = "El distrito es obligatorio."
            tipoDocumento.isBlank() -> errorMsg = "El tipo de documento es obligatorio."
            numeroDocumento.isBlank() -> errorMsg = "El número de documento es obligatorio."
            genero.isBlank() -> errorMsg = "El género es obligatorio."
        }

        if (errorMsg != null) {
            _registerUiState.value = RegisterUiState.Error(RegisterError.EMPTY_CREDENTIALS, errorMsg)
            return
        }

        _registerUiState.value = RegisterUiState.Registering
        viewModelScope.launch {
            val finalProfileData = formData.toMutableMap()
            if (!finalProfileData.containsKey("role_description")) {
                finalProfileData["role_description"] = "Paciente"
            }

            val result = registerRepository.register(email, password, finalProfileData.toMap())
            when (result) {
                is RegisterResult.Success -> _registerUiState.value = RegisterUiState.Success()
                is RegisterResult.Failure -> _registerUiState.value = RegisterUiState.Error(result.errorType, result.message)
            }
        }
    }

    private fun isValidBirthDateFormat(dateStr: String): Boolean {
        return dateStr.matches(Regex("^\\d{4}-\\d{2}-\\d{2}$"))
    }

    private fun isOldEnough(dateStr: String): Boolean {
        if (!isValidBirthDateFormat(dateStr)) return false // Asume formato válido para esta comprobación
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            sdf.isLenient = false
            val birthDate = sdf.parse(dateStr) ?: return false

            val eighteenYearsAgo = Calendar.getInstance().apply { add(Calendar.YEAR, -18) }
            !birthDate.after(eighteenYearsAgo.time) // True si birthDate NO es DESPUÉS de "hoy - 18 años" (es decir, es igual o anterior)
        } catch (e: Exception) {
            false
        }
    }

    fun resetRegisterState() {
        if (_registerUiState.value is RegisterUiState.Success || _registerUiState.value is RegisterUiState.Error) {
            _registerUiState.value = RegisterUiState.Idle
        }
    }
}