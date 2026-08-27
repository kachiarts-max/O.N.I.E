import java.util.Properties

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")

if (localPropertiesFile.exists()) {
    localPropertiesFile.inputStream().use {
        localProperties.load(it)
    }
}

val geminiApiKey = localProperties.getProperty("gemini.api.key", "")

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.onie.assistant"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.onie.assistant"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"

        buildConfigField(
            "String",
            "GEMINI_API_KEY",
            "\"$geminiApiKey\""
        )
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(
                org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17
            )
        }
    }
}

dependencies {

    // AndroidX Activity
    implementation("androidx.activity:activity-compose:1.11.0")

    // AndroidX Core
    implementation("androidx.core:core-ktx:1.17.0")

    // Jetpack Compose
    val composeBom =
        platform("androidx.compose:compose-bom:2026.04.01")

    implementation(composeBom)

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")

    implementation("androidx.compose.material3:material3")

    implementation("androidx.compose.material:material-icons-extended")

    // Kotlin Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")

    // Networking
    implementation("com.squareup.okhttp3:okhttp:5.1.0")

    // Testing
    androidTestImplementation(composeBom)
}
