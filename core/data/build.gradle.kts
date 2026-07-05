import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

// Admin email lives in a gitignored local file (repo is public) — same pattern as
// keystore.properties. Empty string if missing, which just disables the admin panel.
val adminProps = Properties().apply {
    val f = rootProject.file("admin.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}

android {
    namespace = "com.kindred.core.data"
    compileSdk = 36

    defaultConfig {
        minSdk = 26
        buildConfigField("String", "ADMIN_EMAIL", "\"${adminProps.getProperty("adminEmail", "")}\"")
    }

    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.cloudinary.android)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
}
