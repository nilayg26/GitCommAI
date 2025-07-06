import java.util.Properties

plugins {
    kotlin("plugin.serialization") version "2.0.0"
    id("com.google.devtools.ksp") version "2.0.21-1.0.27" apply false
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.android.libraries.mapsplatform.secrets.gradle.plugin)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.gitcommai"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.example.gitcommai"
        minSdk = 29
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        android.buildFeatures.buildConfig=true
        val properties= Properties()
        properties.load(project.rootProject.file("local.properties").inputStream())
        buildConfigField("String","newsApiKey", "\"${properties.getProperty("newsApiKey")}\"")
        buildConfigField("String","privateKeys", "\"${properties.getProperty("privateKeys")}\"")
        buildConfigField("String","publicKeys", "\"${properties.getProperty("publicKeys")}\"")
        buildConfigField("String","rsaData", "\"${properties.getProperty("rsaData")}\"")
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    signingConfigs {
        create("release") {
            storeFile = file("/Users/nilaygupta/release/keystores/gitcommairelease.jks")
            storePassword = "android"
            keyAlias = "gitcommairelease"
            keyPassword = "android"
        }
    }


    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
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
        buildConfig = true
    }
}

dependencies {
    implementation(libs.kotlinx.serialization.json)
    implementation (libs.text.recognition)
    implementation (libs.compose)
    implementation(libs.gson)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.firebase.auth)
    implementation(libs.lottie.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.generativeai)
    implementation(libs.androidx.ui.text.google.fonts)
    implementation(libs.firebase.database)
    implementation(libs.firebase.firestore)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}