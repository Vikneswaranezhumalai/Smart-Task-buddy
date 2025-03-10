plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("kotlin-kapt")
    kotlin("plugin.serialization") version "1.9.10"
}

android {
    namespace = "com.txstate.taskbuddy"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.txstate.taskbuddy"
        minSdk = 33
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        javaCompileOptions {
            annotationProcessorOptions {
                argument("room.schemaLocation", "$projectDir/schemas")
            }
        }

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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    //implementation ("androidx.fragment:fragment-ktx:1.8.5") // For Fragment and Kotlin extensions
    //Retrofit Api Call
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.okhttp3:logging-interceptor:4.9.3'")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")

    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:5.0.0-alpha.2")
    // Jetpack Compose
    implementation ("androidx.compose.ui:ui:1.6.0")
    implementation ("androidx.compose.material:material:1.6.0")
    implementation ("androidx.activity:activity-compose:1.7.2")
    implementation ("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.compose.material3:material3:1.0.0")
    implementation ("androidx.compose.material:material-icons-extended:1.4.0")
    // Room Dependencies
    implementation ("androidx.room:room-runtime:2.4.0")
    implementation ("androidx.room:room-ktx:2.4.0")
    kapt ("androidx.room:room-compiler:2.5.0") // Room annotation processor for KAPT

    // WorkManager Dependency
    implementation ("androidx.work:work-runtime-ktx:2.7.0")
    // Notification
    implementation ("androidx.core:core-ktx:1.7.0")
    // LiveData support for Compose
    implementation ("androidx.compose.runtime:runtime-livedata:1.0.0")
    implementation (libs.androidx.fragment)
    implementation (libs.androidx.viewmodel)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.appcompat)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)





}