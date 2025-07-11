package com.example.sisvitag2.data.model

import com.google.firebase.firestore.PropertyName

data class UserProfileData(
    val uid: String = "",
    val email: String? = "",
    val displayName: String? = "", // Nombre completo para mostrar (puede ser de Auth o construido)
    val photoUrl: String? = null,  // URL de la foto de perfil desde Firebase Storage/Auth

    // Campos de tu colección 'personas'
    @get:PropertyName("nombre") @set:PropertyName("nombre")
    var nombre: String? = "",
    @get:PropertyName("apellidopaterno") @set:PropertyName("apellidopaterno")
    var apellidoPaterno: String? = "",
    @get:PropertyName("apellidomaterno") @set:PropertyName("apellidomaterno")
    var apellidoMaterno: String? = "",
    @get:PropertyName("fechanacimiento") @set:PropertyName("fechanacimiento")
    var fechaNacimiento: com.google.firebase.Timestamp? = null, // Ahora es Timestamp
    @get:PropertyName("tipo_documento") @set:PropertyName("tipo_documento")
    var tipoDocumento: String? = "",
    @get:PropertyName("numero_documento") @set:PropertyName("numero_documento")
    var numeroDocumento: String? = "",
    @get:PropertyName("genero") @set:PropertyName("genero")
    var genero: String? = "",
    val telefono: String? = "",

    // Campos de ubicación (nombres, no IDs)
    @get:PropertyName("departamento") @set:PropertyName("departamento")
    var departamento: String? = "",
    @get:PropertyName("provincia") @set:PropertyName("provincia")
    var provincia: String? = "",
    @get:PropertyName("distrito") @set:PropertyName("distrito")
    var distrito: String? = "",
    // Nuevos campos para estado y rol
    @get:PropertyName("estado") @set:PropertyName("estado")
    var estado: String? = null,
    @get:PropertyName("tipousuarioid") @set:PropertyName("tipousuarioid")
    var tipousuarioid: String? = null,
    @get:PropertyName("legacyTipoUsuarioId") @set:PropertyName("legacyTipoUsuarioId")
    var legacyTipoUsuarioId: Int? = null,
    @get:PropertyName("ubigeoid") @set:PropertyName("ubigeoid")
    var ubigeoid: String? = null
)