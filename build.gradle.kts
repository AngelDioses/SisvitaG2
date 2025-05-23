// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    // alias(libs.plugins.kotlin.compose) apply false // <-- LÍNEA ELIMINADA/COMENTADA (CORRECCIÓN)
    // Mantener si usas Maps:
    // alias(libs.plugins.google.android.libraries.mapsplatform.secrets.gradle.plugin) apply false
    // Añadir si usas Kotlinx Serialization:
    // alias(libs.plugins.kotlin.serialization) apply false
    id("com.google.gms.google-services") version "4.4.2" apply false
}