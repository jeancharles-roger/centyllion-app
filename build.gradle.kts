val grpUser: String by project
val grpToken: String by project

val debug: String? by project
val d = debug?.toBoolean() ?: false

val serializationVersion: String = "1.6.0"
val coroutineVersion: String = "1.7.22"
val kotlinxHtmlVersion: String = "0.7.3"
val bulmaKotlinVersion: String = "0.5"
val babylonKotlinVersion: String = "0.5.2"
val data2vizVersion: String = "0.10.1"
val markdownVersion: String = "0.5.2"

plugins {
    kotlin("multiplatform") version "1.9.20" apply false
    kotlin("plugin.serialization") version "1.9.20" apply false
    id("org.jetbrains.compose") version "1.5.10" apply false
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

