plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.securepoint"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.securepoint"
        minSdk = 30
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        viewBinding =true

    }
}


dependencies {
    implementation("androidx.navigation:navigation-compose:2.5.3")
    implementation("androidx.compose.ui:ui:1.5.0")
    implementation("androidx.compose.material:material:1.5.0")
    implementation( platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation ("androidx.appcompat:appcompat:1.6.1")
    implementation ("com.google.android.material:material:1.9.0")
    implementation ("com.google.firebase:firebase-auth:22.1.1")  // Latest Firebase Auth
    implementation ("com.google.firebase:firebase-database:20.2.2")  // (For Firebase Realtime Database)
    implementation ("com.google.firebase:firebase-messaging:23.3.1")
    implementation ("androidx.preference:preference:1.2.1")
    implementation("com.google.gms:google-services:4.4.1")
    implementation ("com.google.firebase:firebase-auth")  // Firebase Authentication
    implementation ("com.google.firebase:firebase-database")  // Firebase Realtime Database
    implementation ("com.google.firebase:firebase-messaging")  // Firebase Cloud Messaging
    implementation ("com.google.firebase:firebase-storage")  // Firebase Storage (Optional)
    implementation ("com.github.bumptech.glide:glide:4.15.1")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.15.1")
    implementation ("androidx.gridlayout:gridlayout:1.0.0") 
    implementation ("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation ("com.itextpdf:itext7-core:7.2.2") // Free version of iText 7
    implementation ("com.google.firebase:firebase-messaging:23.4.1") // Use latest version

}