val grpUser: String by project
val grpToken: String by project

val debug: String? by project
val d = debug?.toBoolean() ?: false

val serializationVersion: String = "1.2.2"
val coroutineVersion: String = "1.4.2"
val kotlinxHtmlVersion: String = "0.7.3"
val bulmaKotlinVersion: String = "0.5"
val babylonKotlinVersion: String = "0.5.2"
val data2vizVersion: String = "0.10.1"
val markdownVersion: String = "0.2.4"

plugins {
    kotlin("multiplatform") version "1.9.0" apply false
    kotlin("plugin.serialization") version "1.9.0" apply false
}

allprojects {
    repositories {
        mavenCentral()
    }
    group = "com.centyllion"
    version = "0.0.1"
}

