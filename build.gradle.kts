// Top-level build file (TaskRabbit/build.gradle.kts)

// You can keep these variable definitions for documentation if you want,
// but they CANNOT be used directly inside the plugins { } block below.
// val agpVersion = "8.9.1"
// val kotlinVersion = "2.0.21"
// val kspVersion = "2.0.21-1.0.27"

plugins {
    // Use explicit IDs and put version strings DIRECTLY here
    id("com.android.application") version "8.9.1" apply false // Use string literal
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false // Use string literal
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21" apply false // Use string literal
    id("com.google.devtools.ksp") version "2.0.21-1.0.27" apply false // Use string literal
}