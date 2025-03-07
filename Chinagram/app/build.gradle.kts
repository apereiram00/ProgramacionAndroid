plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    viewBinding {
        enable = true
    }

    namespace = "com.example.chinagram"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.chinagram"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
}

dependencies {
    implementation (libs.media3.exoplayer)
    implementation (libs.media3.ui)
    implementation (libs.google.firebase.auth.v2231)
    implementation (libs.firebase.firestore)
    implementation (libs.exoplayer)
    implementation(libs.okhttp) // OkHttp para peticiones HTTP a Supabase
    implementation(libs.gson) // Gson para parsear JSON
    implementation(libs.glide) // Glide para cargar imágenes
    implementation(libs.play.services.auth) // Google Play Services para autenticación
    implementation(libs.firebase.auth) // Firebase Auth
    implementation(libs.room.runtime) // Room para la base de datos local
    annotationProcessor(libs.room.compiler) // Procesador de anotaciones para Room
    implementation(platform(libs.firebase.bom)) // BOM de Firebase para gestionar versiones
    implementation(libs.appcompat) // AppCompat para compatibilidad
    implementation(libs.material) // Material Design
    implementation(libs.activity) // Activity Kotlin Extensions
    implementation(libs.constraintlayout) // ConstraintLayout
    implementation(libs.navigation.fragment) // Navigation Fragment
    implementation(libs.navigation.ui) // Navigation UI
    testImplementation(libs.junit) // JUnit para pruebas unitarias
    androidTestImplementation(libs.ext.junit) // JUnit para pruebas Android
    androidTestImplementation(libs.espresso.core) // Espresso para pruebas UI
}