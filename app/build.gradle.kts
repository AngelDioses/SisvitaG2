// En app/build.gradle.kts

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.example.sisvitag2"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.sisvitag2"
        minSdk = 28
        targetSdk = 35
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    // Configuración de lint para evitar el error de compilación
    lint {
        disable += "StateFlowValueCalledInComposition"
        checkReleaseBuilds = false
        abortOnError = false
    }

    // composeOptions { // Comentado o eliminado si la BOM lo maneja
    //     kotlinCompilerExtensionVersion = "..."
    // }
}

dependencies {
    // --- Core y Lifecycle ---
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)      // Versión gestionada por BOM de Compose/Lifecycle
    implementation(libs.androidx.lifecycle.viewmodel.compose) // Versión gestionada por BOM de Compose/Lifecycle
    implementation(libs.androidx.activity.compose)          // Versión gestionada por BOM de Compose/Lifecycle

    // --- Compose UI (BOM) ---
    implementation(platform(libs.androidx.compose.bom)) // La versión está en libs.versions.toml
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.core)      // Iconos base de Material
    implementation(libs.androidx.material.icons.extended) // Iconos extendidos

    implementation("androidx.activity:activity:1.8.1")

    // --- Firebase (BOM) ---
    implementation(platform(libs.firebase.bom)) // Usar alias si definiste firebase.bom en [libraries]
    // o la string directa como tenías: platform("com.google.firebase:firebase-bom:33.1.0")
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.firebase.storage.ktx)
    implementation(libs.firebase.functions.ktx)
    implementation("com.google.firebase:firebase-analytics") // Puedes dejarlo así o catalogarlo

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
    implementation(libs.androidx.navigation.runtime.ktx)
    implementation(libs.androidx.navigation.compose)

    // --- Kotlinx Serialization ---
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.media3.common.ktx)

    // --- OkHttp para llamadas a API ---
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    
    // --- Google AI (Gemini) ---
    implementation("com.google.ai.client.generativeai:generativeai:0.1.2")



    // --- Dependencias Opcionales (Eliminar si no se usan) ---
    // implementation(libs.volley)
    // implementation(libs.androidx.room.runtime.android)

    // --- AudioWaveform ---
    implementation(libs.audiowaveform)

    // --- Coil ---
    implementation(libs.coil.compose)
    // implementation(libs.coil.gif) // Opcional

    // --- Test ---
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}