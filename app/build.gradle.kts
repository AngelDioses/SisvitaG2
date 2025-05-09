// En app/build.gradle.kts

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose) // Usamos el alias vinculado a Kotlin
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.kotlin.serialization)
    // ----------------------------------
}

android {
    namespace = "com.example.sisvitag2" // Tu namespace
    compileSdk = 35 // O 35

    defaultConfig {
        applicationId = "com.example.sisvitag2" // Tu ID
        minSdk = 28
        targetSdk = 35 // O 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        // Kotlin 2.1.0 funciona bien con Java 11, pero Java 17 es más moderno
        sourceCompatibility = JavaVersion.VERSION_11 // O JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_11 // O JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "11" // O "17"
    }
    buildFeatures {
        compose = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14" // <-- ¡¡PUEDE NECESITAR CAMBIO!!
    }
}

dependencies {
    // --- Core y Lifecycle ---
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)

    // --- Compose UI (BOM) ---
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    // --- Firebase (BOM) ---
    implementation(platform("com.google.firebase:firebase-bom:33.13.0"))
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.firebase.storage.ktx)
    implementation(libs.firebase.functions.ktx)
    implementation("com.google.firebase:firebase-analytics")

    // --- Koin ---
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)

    // --- Coroutines Play Services ---
    implementation(libs.kotlinx.coroutines.play.services)

    // --- Lottie ---
    implementation(libs.lottie.compose)

    // --- CameraX ---
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.video)
    implementation(libs.androidx.camera.view)

    // --- Navigation ---
    implementation(libs.androidx.navigation.runtime.ktx) // KTX version
    implementation(libs.androidx.navigation.compose)

    // --- Kotlinx Serialization ---
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.media3.common.ktx) // <-- Añadida dependencia
    // ---------------------------

    // --- Test ---
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation(libs.audiowaveform) // <-- Añadir esta línea

    implementation(libs.androidx.material.icons.extended) // Si usas el catálogo de versiones
}