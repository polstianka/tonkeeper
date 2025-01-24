@file:Suppress("UnstableApiUsage")
import com.android.build.api.dsl.ManagedVirtualDevice

plugins {
    id("com.android.test")
    id("org.jetbrains.kotlin.android")
    id("androidx.baselineprofile")
}

val isCI = project.hasProperty("android.injected.signing.store.file")

android {
    namespace = Build.namespacePrefix("main.baselineprofile")
    compileSdk = Build.compileSdkVersion

    defaultConfig {
        testInstrumentationRunnerArguments += mapOf("suppressErrors" to "EMULATOR")
        minSdk = 28
        targetSdk = Build.compileSdkVersion
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    testOptions.managedDevices.devices {
        create<ManagedVirtualDevice>("pixel6Api33") {
            device = "Pixel 6"
            apiLevel = 33
            systemImageSource = "google"
        }
    }

    targetProjectPath = ":apps:wallet:instance:main"

    experimentalProperties["android.experimental.self-instrumenting"] = true
    experimentalProperties["android.experimental.testOptions.managedDevices.setupTimeoutMinutes"] = 20
    experimentalProperties["android.experimental.androidTest.numManagedDeviceShards"] = 1
    experimentalProperties["android.experimental.testOptions.managedDevices.maxConcurrentDevices"] = 1
    experimentalProperties["android.experimental.testOptions.managedDevices.emulator.showKernelLogging"] = true
    if (isCI) {
        experimentalProperties["android.testoptions.manageddevices.emulator.gpu"] = "swiftshader_indirect"
    }
}

dependencies {
    implementation("androidx.test.ext:junit:1.2.1")
    implementation("androidx.test.espresso:espresso-core:3.6.1")
    implementation("androidx.test.uiautomator:uiautomator:2.3.0")
    implementation("androidx.benchmark:benchmark-macro-junit4:1.3.3")
}

baselineProfile {
    // managedDevices += "pixel6Api33"
    // useConnectedDevices = false
    enableEmulatorDisplay = !isCI
}

androidComponents {
    onVariants { v ->
        val artifactsLoader = v.artifacts.getBuiltArtifactsLoader()
        val testedApks = v.testedApks.map {
            artifactsLoader.load(it)?.applicationId ?: "com.ton_keeper"
        }
        v.instrumentationRunnerArguments.put("targetAppId", testedApks)
    }
}