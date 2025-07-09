package com.example.sisvitag2.data.model

data class UserProfileData(
    val uid: String = "",
    val email: String? = "",
    val displayName: String? = "", // Nombre completo para mostrar (puede ser de Auth o construido)
    val photoUrl: String? = null,  // URL de la foto de perfil desde Firebase Storage/Auth

    // Campos de tu colección 'personas'
    val nombre: String? = "",
    val apellidoPaterno: String? = "",
    val apellidoMaterno: String? = "",
    val fechaNacimiento: String? = "", // Formateada como String para mostrar (ej. "dd/MM/yyyy")
    val tipoDocumento: String? = "",
    val numeroDocumento: String? = "",
    val genero: String? = "",
    val telefono: String? = "",

    // Campos de ubicación (nombres, no IDs)
    val departamento: String? = "",
    val provincia: String? = "",
    val distrito: String? = "",
    // Nuevos campos para estado y rol
    val estado: String? = null,
    val legacyTipoUsuarioId: Int? = null
)