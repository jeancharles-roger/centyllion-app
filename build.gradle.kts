val grpUser: String by project
val grpToken: String by project

val debug: String? by project
val d = debug?.toBoolean() ?: false

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
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        google()
    }
    group = "com.centyllion"
    version = "0.0.1"
}

