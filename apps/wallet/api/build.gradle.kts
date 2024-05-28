plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("kotlin-parcelize")
    id("kotlinx-serialization")
}

android {
    namespace = Build.namespacePrefix("wallet.api")
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
    implementation(Dependence.Koin.core)
    implementation(project(Dependence.Module.tonApi))
    implementation(project(Dependence.Module.stonfiApi))
    implementation(project(Dependence.Lib.network))
    implementation(project(Dependence.Lib.blockchain))
    implementation(project(Dependence.Lib.extensions))
    implementation(Dependence.Squareup.okhttp)
    implementation(Dependence.Squareup.sse)
    implementation(Dependence.Squareup.moshi)
    implementation(Dependence.Squareup.moshiAdapters)
}
