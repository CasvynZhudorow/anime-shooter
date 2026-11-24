plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.animeshooter"
    compileSdk = 34
    
    defaultConfig {
        applicationId = "com.animeshooter"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
        
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        ndk {
            abiFilters += listOf("arm64-v8a", "armeabi-v7a", "x86", "x86_64")
        }
    }
    
    signingConfigs {
        create("release") {
            storeFile = file("keystore.jks")
            storePassword = System.getenv("KEYSTORE_PASSWORD") ?: "android"
            keyAlias = System.getenv("KEY_ALIAS") ?: "starfall"
            keyPassword = System.getenv("KEY_PASSWORD") ?: "android"
        }
    }
    
    buildTypes {
        getByName("debug") {
            isDebuggable = true
        }
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    
    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += listOf("-Xjvm-default=all", "-Xopt-in=kotlin.RequiresOptIn")
    }
    
    packagingOptions {
        resources {
            excludes += listOf("META-INF/*.kotlin_module")
        }
    }
}

dependencies {
    implementation(project(":core"))
    implementation(libs.libgdx.backend.android)
    implementation(libs.libgdx.platform)
    implementation(libs.libgdx.box2d.platform)
    implementation(libs.libgdx.freetype.platform)
    
    androidTestImplementation(libs.runner)
    androidTestImplementation(libs.rules)
    androidTestImplementation(libs.espresso.core)
}

tasks {
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            jvmTarget = "17"
            freeCompilerArgs += listOf("-Xjvm-default=all", "-Xopt-in=kotlin.RequiresOptIn")
        }
    }
}