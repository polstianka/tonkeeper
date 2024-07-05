plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("androidx.baselineprofile")
}

android {
    namespace = Build.namespacePrefix("signer")
    compileSdk = Build.compileSdkVersion

    defaultConfig {
        applicationId = Build.namespacePrefix("signer")
        minSdk = Build.minSdkVersion
        targetSdk = 34
        versionCode = 11
        versionName = "0.1.1"
    }

    lint {
        baseline = file("lint-baseline.xml")
    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false
            isJniDebuggable = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("String", "ENVIRONMENT", "\"\"")
        }

        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            buildConfigField("String", "ENVIRONMENT", "\"dev\"")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    packaging {
        resources.excludes.add("/META-INF/{AL2.0,LGPL2.1}")
    }
}

dependencies {
    implementation(Dependence.AndroidX.profileinstaller)
    "baselineProfile"(project(":baselineprofile:signer"))

    implementation(Dependence.AndroidX.core)
    implementation(Dependence.AndroidX.appCompat)
    implementation(Dependence.AndroidX.activity)
    implementation(Dependence.AndroidX.fragment)
    implementation(Dependence.AndroidX.recyclerView)
    implementation(Dependence.AndroidX.viewPager2)
    implementation(Dependence.AndroidX.splashscreen)

    implementation(Dependence.UI.material)
    implementation(Dependence.AndroidX.Camera.base)
    implementation(Dependence.AndroidX.Camera.core)
    implementation(Dependence.AndroidX.Camera.lifecycle)
    implementation(Dependence.AndroidX.Camera.view)
    implementation(Dependence.AndroidX.security)
    implementation(Dependence.AndroidX.lifecycleSavedState)
    api(project(Dependence.Lib.blockchain))


    implementation(project(Dependence.UIKit.core)) {
        exclude("com.airbnb.android", "lottie")
        exclude("com.facebook.fresco", "fresco")
    }

    implementation(project(Dependence.Lib.qr))
    implementation(project(Dependence.Lib.security))
    implementation(project(Dependence.Lib.icu))
    implementation(Dependence.Koin.core)
}

