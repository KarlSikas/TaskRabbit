plugins {
    alias(libs.plugins.ksp)
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.taskrabbit"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.taskrabbit"
        minSdk = 23
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Adding the build timestamp as a BuildConfig field
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
        buildConfig = true // Ensure this is enabled to generate BuildConfig
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.6.0"  // Update to the latest stable version
    }

    dependencies {
        // Compose BOM (platform to share versions)
        implementation(platform(libs.androidx.compose.bom))

        // Compose dependencies
        implementation(libs.androidx.compose.ui)
        implementation(libs.androidx.compose.ui.graphics)
        implementation(libs.androidx.compose.ui.tooling.preview)
        implementation(libs.androidx.compose.material3)

        // Lifecycle & activity
        implementation(libs.androidx.lifecycle.runtime.ktx)
        implementation(libs.androidx.activity.compose)

        // LiveData & ViewModel for Jetpack Compose
        implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.4.0")  // ViewModel with Compose support
        implementation("androidx.compose.runtime:runtime-livedata:1.0.0")  // For observeAsState

        // Core
        implementation(libs.androidx.core.ktx)

        // Coil
        implementation("io.coil-kt:coil-compose:2.6.0")

        // Room
        implementation("androidx.room:room-runtime:2.6.1")
        implementation("androidx.room:room-ktx:2.6.1")
        ksp("androidx.room:room-compiler:2.6.1")
        androidTestImplementation("androidx.room:room-testing:2.6.1")

        // Coroutines
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")

        // Date/time library
        implementation("com.jakewharton.threetenabp:threetenabp:1.4.6")

        // Compose Testing
        androidTestImplementation(platform(libs.androidx.compose.bom))
        androidTestImplementation(libs.androidx.compose.ui.test.junit4)
        debugImplementation(libs.androidx.compose.ui.tooling)
        debugImplementation(libs.androidx.compose.ui.test.manifest)

        // Unit tests
        testImplementation(libs.junit)
        androidTestImplementation(libs.androidx.junit)
        androidTestImplementation(libs.androidx.espresso.core)

        // Material Icons (consider updating to stable version if possible)
        implementation("androidx.compose.material:material-icons-extended:1.7.0")

        // Navigation
        implementation("androidx.navigation:navigation-compose:2.7.7")

        implementation("io.coil-kt:coil-compose:2.1.0")
    }
}
