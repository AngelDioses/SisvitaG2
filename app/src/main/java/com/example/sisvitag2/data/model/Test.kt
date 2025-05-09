package com.example.sisvitag2.data.model

import com.google.firebase.firestore.DocumentId

data class Test(
    @DocumentId var id: String = "",
    val nombre: String = "",
    val descripcion: String = ""
)

//ED para enviar a la CF que procesa el test
data class TestSubmission(

    val testId: String = "", // ID (String) del documento 'Test' que se realizó
    val respuestas: List<RespuestaSubmission> = emptyList()
)

//Representa una unica respuesta dada por el usuario
data class RespuestaSubmission(
    val preguntaId: String = "", // ID (String) del documento 'Pregunta'
    val respuestaId: String = "" // ID (String) del documento 'Respuesta' seleccionada
)
