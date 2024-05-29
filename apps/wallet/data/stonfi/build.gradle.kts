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
    implementation(Dependence.ton)
    implementation(project(Dependence.Module.ton))
    implementation(project(Dependence.Module.tonApi))
    implementation(project(Dependence.Module.stonApi))
    implementation(project(Dependence.Wallet.Data.core))
    implementation(project(Dependence.Wallet.Data.rates))
    implementation(project(Dependence.Wallet.api))
    implementation(project(Dependence.Lib.extensions))
}
