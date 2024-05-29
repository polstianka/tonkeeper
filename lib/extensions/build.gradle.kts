plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("kotlin-parcelize")
}

android {
    namespace = Build.namespacePrefix("extensions")
    compileSdk = Build.compileSdkVersion

    defaultConfig {
        minSdk = Build.minSdkVersion
        consumerProguardFiles("consumer-rules.pro")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation(Dependence.KotlinX.coroutines)
    implementation(Dependence.Koin.core)
    implementation(Dependence.AndroidX.core)
    implementation(project(Dependence.UIKit.core))
    implementation(project(Dependence.UIKit.icon))
    implementation(project(Dependence.Lib.icu))
}
