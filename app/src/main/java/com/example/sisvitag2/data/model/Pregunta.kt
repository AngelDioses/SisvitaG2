package com.example.sisvitag2.data.model

import com.google.firebase.firestore.DocumentId

data class Pregunta(
    @DocumentId var id: String = "",
    val testId: String = "",
    val textoPregunta: String = "",
    val numeroPregunta: Int = 0
)
