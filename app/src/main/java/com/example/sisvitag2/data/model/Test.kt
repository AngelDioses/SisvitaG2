package com.example.sisvitag2.data.model

import com.google.firebase.firestore.DocumentId

data class Test(
    @DocumentId var id: String = "", // Correcto, se mapeará al ID del documento
    val nombre: String = "",
    val descripcion: String = ""
    // legacyTestId no está en tu data class Test. ¿Lo necesitas en la app?
)

//ED para enviar a la CF que procesa el test
data class TestSubmission(
    // El ID de la persona (usuario) se obtendrá del contexto de autenticación
    // en la Cloud Function (context.auth.uid), por lo que no es estrictamente
    // necesario enviarlo desde el cliente, a menos que la función lo requiera explícitamente.
    // val personaUid: String,

    val testId: String = "", // ID (String) del documento 'Test' que se realizó
    val respuestas: List<RespuestaSubmission> = emptyList()
)

//Representa una unica respuesta dada por el usuario
data class RespuestaSubmission(
    val preguntaId: String = "", // ID (String) del documento 'Pregunta'
    val respuestaId: String = "" // ID (String) del documento 'Respuesta' seleccionada
)
