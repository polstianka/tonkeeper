plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("kotlin-parcelize")
}

android {
    namespace = Build.namespacePrefix("wallet.data.stonfi")
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
    implementation(Dependence.Squareup.okhttp)
    implementation(Dependence.Squareup.logger)
    implementation(Dependence.Squareup.retrofit)
    implementation(Dependence.Squareup.moshi)
    implementation(Dependence.Squareup.moshiAdapters)
    implementation(Dependence.Squareup.retrofitMoshiAdapter)
    implementation(project(Dependence.Lib.extensions))
}
