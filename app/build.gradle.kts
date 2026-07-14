import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.jetbrains.dokka)
    id("kotlin-kapt")
}

// Load local properties
val localProperties = Properties()
val localPropertiesFile = project.rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(FileInputStream(localPropertiesFile))
}

android {
    namespace = "com.resistine.android"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.resistine.android"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Read the API key from local.properties
        val apiKey = localProperties.getProperty("OPENAI_API_KEY") ?: ""
        buildConfigField("String", "OPENAI_API_KEY", "\"$apiKey\"")
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
        isCoreLibraryDesugaringEnabled = true
    }

    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    kapt {
        correctErrorTypes = true
    }

    // Dokka V2 configuration for multiple formats
//    dokka {
//        dokkaPublications.register("gfm") {
//            // For generating Markdown (GFM)
//        }
//    }
}

tasks.register<Exec>("buildWireGuardTelemetryNative") {
    group = "build"
    description = "Builds the telemetry-enabled wireguard-go library for all Android ABIs."
    workingDir(rootProject.projectDir)
    commandLine(
        "powershell.exe",
        "-NoProfile",
        "-ExecutionPolicy",
        "Bypass",
        "-File",
        rootProject.file("tools/build_wireguard_telemetry.ps1").absolutePath
    )
}

val wireGuardTelemetryAbis = listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
val verifyWireGuardTelemetryNative by tasks.registering {
    group = "verification"
    description = "Verifies that every supported ABI has the pinned telemetry backend."
    val libraries = wireGuardTelemetryAbis.map { abi ->
        layout.projectDirectory.file("src/main/jniLibs/$abi/libwg-go-telemetry.so")
    }
    inputs.files(libraries)
    doLast {
        libraries.forEach { library ->
            val file = library.asFile
            check(file.isFile && file.length() >= 1_000_000L) {
                "Missing or invalid WireGuard telemetry backend: ${file.absolutePath}"
            }
        }
    }
}

tasks.named("preBuild").configure {
    dependsOn(verifyWireGuardTelemetryNative)
}

dependencies {
    implementation(libs.firebase.crashlytics.buildtools)
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")
    implementation("com.wireguard.android:tunnel:1.0.20260102")
    implementation("androidx.recyclerview:recyclerview:1.4.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.navigation.fragment.ktx)
    implementation(libs.navigation.ui.ktx)
    implementation(libs.play.services.location)

    // Room
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    kapt("androidx.room:room-compiler:$roomVersion")

    // WorkManager
    val workVersion = "2.9.1"
    implementation("androidx.work:work-runtime-ktx:$workVersion")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Dokka GFM (Markdown) support
//    dokkaPlugin(libs.dokka.gfm.plugin)
}
