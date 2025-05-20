plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("org.jetbrains.kotlin.kapt")
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.0"
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.otriviafan"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.otriviafan"
        minSdk = 26
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.10"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

}

dependencies {
// Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")



// Firebase
   /* implementation(libs.firebase.crashlytics.buildtools)
    implementation(libs.play.services.maps)
    implementation(libs.androidx.runner)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.firebase.appdistribution.gradle)
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.database.ktx)*/

    implementation(libs.firebase.firestore.ktx) {
        exclude(group = "com.google.api.grpc", module = "proto-google-common-protos")
    }
    implementation(libs.firebase.auth.ktx) {
        exclude(group = "com.google.api.grpc", module = "proto-google-common-protos")
    }
    implementation(libs.firebase.database.ktx) {
        exclude(group = "com.google.api.grpc", module = "proto-google-common-protos")
    }

// Navigation
    implementation("androidx.navigation:navigation-compose:2.7.7")

// Compose
    implementation("androidx.compose.ui:ui:1.6.1")
    implementation("androidx.compose.material3:material3:1.1.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("io.coil-kt:coil-compose:2.4.0")

// Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.google.code.gson:gson:2.10.1")

// Otros
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation("nl.joery.animatedbottombar:library:1.1.0")
    implementation("nl.dionsegijn:konfetti-compose:2.0.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.1")
    implementation("androidx.compose.ui:ui-text-google-fonts:1.6.0")

// Tests
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

}
configurations.all {
    resolutionStrategy {
        force("com.google.api.grpc:proto-google-common-protos:2.48.0")
    }
}


