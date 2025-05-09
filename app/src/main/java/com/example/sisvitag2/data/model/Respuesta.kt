package com.example.sisvitag2.data.model

import com.google.firebase.firestore.DocumentId

data class Respuesta(
    @DocumentId var id: String = "",
    val testId: String = "", // Este DEBE COINCIDIR con el ID del DOCUMENTO del test
    val textoRespuesta: String = "",
    val numeroRespuesta: Int = 0
)
