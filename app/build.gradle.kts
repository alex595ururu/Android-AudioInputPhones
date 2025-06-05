plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

dependencies {
    //  This is for androidx.annotation.NonNull class for Java
    //  implementation(libs.androidx.annotation.jvm)
}

android {
    namespace = "com.androidActivity"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.androidActivity"
        minSdk = 24
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildToolsVersion = "36.0.0"

    //  Uncomment this if using Kotlin
    kotlinOptions {
        jvmTarget = "17"
    }
}
