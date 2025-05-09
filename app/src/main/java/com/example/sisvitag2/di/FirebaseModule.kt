package com.example.sisvitag2.di


// --- Importaciones Correctas ---
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.functions.ktx.functions
import com.google.firebase.storage.ktx.storage
// --- Fin Importaciones ---
import org.koin.dsl.module

val firebaseModule = module {
    // Proporciona una instancia Singleton de FirebaseAuth
    single<FirebaseAuth> { Firebase.auth }

    // Proporciona una instancia Singleton de FirebaseFirestore
    single<FirebaseFirestore> { Firebase.firestore }

    // Proporciona una instancia Singleton de FirebaseStorage
    single<FirebaseStorage> { Firebase.storage }

    // Proporciona una instancia Singleton de FirebaseFunctions
    single<FirebaseFunctions> { Firebase.functions } // O Firebase.functions("tu-region") si es necesario
}