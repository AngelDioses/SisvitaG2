package com.example.sisvitag2.data.model

import com.google.firebase.firestore.DocumentId

data class Respuesta(
    @DocumentId var id: String = "",
    val testId: String = "",
    val textoRespuesta: String = "",
    val numeroRespuesta: Int = 0
)
