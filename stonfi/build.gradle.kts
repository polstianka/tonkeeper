plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "stonfi"
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
    implementation(Dependence.Squareup.moshi)
    implementation(Dependence.Squareup.moshiAdapters)
    implementation(Dependence.Squareup.okhttp)
}
