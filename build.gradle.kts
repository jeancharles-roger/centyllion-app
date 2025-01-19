
plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.compose.jetbrains) apply false
    alias(libs.plugins.compose.compiler) apply false

    // serialization
    alias(libs.plugins.kotlin.serialization) apply false

    // sqldelight
    alias(libs.plugins.sqldelight) apply false

}

allprojects {
    repositories {
        mavenCentral()
        google()
    }
    group = "com.centyllion"
    version = "0.0.1"
}

