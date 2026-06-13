plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.navigation.safeargs.kotlin)
}

android {
    namespace = "com.avetiso.feature_appointments"
    compileSdk = 36

    defaultConfig {
        minSdk = 24
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11

        isCoreLibraryDesugaringEnabled = true
    }

    buildFeatures {
        viewBinding = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }
}

dependencies {

    implementation(project(":core")) // Все фичи зависят от core
    implementation(project(":common_ui"))
    implementation(project(":navigation"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.lifecycle.viewmodel.savedstate)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)

    // ViewModel & Lifecycle
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.fragment.ktx)

    // Hilt (для ViewModel)
    implementation(libs.hilt.android)
    implementation(libs.firebase.crashlytics.buildtools)
    ksp(libs.hilt.compiler)

    // Gson
    implementation(libs.gson)

    // Desugar (десахаризация)
    coreLibraryDesugaring(libs.desugar.jdk.libs)
}