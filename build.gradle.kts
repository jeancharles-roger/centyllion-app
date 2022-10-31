@file:Suppress("UNUSED_VARIABLE")

val debug: String? by project
val d = debug?.toBoolean() ?: false

val serializationVersion: String = "1.3.2"
val coroutineVersion: String = "1.5.2"
val cliktVersion: String = "3.4.0"

val data2vizVersion: String = "0.8.12"
val markdownVersion: String = "0.2.4"

plugins {
    kotlin("jvm") version "1.7.20" apply false
    kotlin("plugin.serialization") version "1.7.20" apply false
    id("org.jetbrains.compose") version "1.2.0" apply false
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven { url = uri("https://maven.pkg.jetbrains.space/data2viz/p/maven/public") }
    }

    group = "com.centyllion"
    version = "0.0.1"
}
