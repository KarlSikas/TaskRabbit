// app/build.gradle.kts - NO libs aliases

// Apply plugins by ID only. Versions come from project-level build file.
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.example.taskrabbit"
    // Use a stable SDK, 34 is generally recommended if targeting 26.
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.taskrabbit"
        minSdk = 26 // <<< Set to 26 as requested
        targetSdk = 26 // <<< Set to 26 as requested
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "BUILD_TIMESTAMP", "\"${System.currentTimeMillis()}\"")
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

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        // CRITICAL: Verify this version based on Kotlin 2.0.21 and your Compose library versions.
        // Check compatibility: https://developer.android.com/jetpack/androidx/releases/compose-kotlin
        // '1.5.21' is likely correct.
        kotlinCompilerExtensionVersion = "1.5.21"
    }
}

// Main dependencies block - uses explicit coordinates (group:name:version)
dependencies {
    // Define versions explicitly (use values from your original libs.versions.toml)
    val composeBomVersion = "2024.09.00"
    val coreKtxVersion = "1.16.0"
    val lifecycleVersion = "2.8.7"
    val activityComposeVersion = "1.10.1"
    val roomVersion = "2.6.1"
    val coroutinesVersion = "1.8.0"
    val threetenabpVersion = "1.4.6"
    val junitVersion = "4.13.2"
    val androidxJunitVersion = "1.2.1"
    val espressoCoreVersion = "3.6.1"
    val navigationVersion = "2.7.7"
    val coilVersion = "2.6.0"
    val datastoreVersion = "1.0.0"
    val splashscreenVersion = "1.0.1"
    val materialIconsVersion = "1.7.0-beta01" // Or a stable version compatible

    // Compose BOM (platform to share versions)
    implementation(platform("androidx.compose:compose-bom:$composeBomVersion"))

    // Compose dependencies (versions managed by BOM)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3") // Version from BOM

    // Lifecycle & activity
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycleVersion")
    implementation("androidx.activity:activity-compose:$activityComposeVersion")

    // LiveData & ViewModel for Jetpack Compose
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:$lifecycleVersion")
    implementation("androidx.compose.runtime:runtime-livedata") // Version from BOM

    // Core
    implementation("androidx.core:core-ktx:$coreKtxVersion")

    // Coil
    implementation("io.coil-kt:coil-compose:$coilVersion")

    // Room
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")
    androidTestImplementation("androidx.room:room-testing:$roomVersion")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVersion")

    // Date/time library
    implementation("com.jakewharton.threetenabp:threetenabp:$threetenabpVersion")

    // Compose Testing (versions managed by BOM)
    androidTestImplementation(platform("androidx.compose:compose-bom:$composeBomVersion"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4") // Version from BOM
    debugImplementation("androidx.compose.ui:ui-tooling") // Version from BOM
    debugImplementation("androidx.compose.ui:ui-test-manifest") // Version from BOM

    // Unit tests
    testImplementation("junit:junit:$junitVersion")
    androidTestImplementation("androidx.test.ext:junit:$androidxJunitVersion")
    androidTestImplementation("androidx.test.espresso:espresso-core:$espressoCoreVersion")

    // Material Icons
    implementation("androidx.compose.material:material-icons-extended:$materialIconsVersion")

    // Navigation
    implementation("androidx.navigation:navigation-compose:$navigationVersion")

    // DataStore
    implementation("androidx.datastore:datastore-preferences:$datastoreVersion")

    // SplashScreen
    implementation("androidx.core:core-splashscreen:$splashscreenVersion")
}