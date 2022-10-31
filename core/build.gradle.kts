@file:Suppress("UNUSED_VARIABLE")

val coroutineVersion: String = "1.5.2"
val serializationVersion: String = "1.3.2"

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

dependencies {
    api("org.jetbrains.kotlinx", "kotlinx-coroutines-core-jvm", coroutineVersion)
    implementation("org.jetbrains.kotlinx", "kotlinx-serialization-core", serializationVersion)
    api("org.jetbrains.kotlinx", "kotlinx-serialization-json", serializationVersion)

    testImplementation(kotlin("test-common"))
    testImplementation(kotlin("test-annotations-common"))
    testImplementation(kotlin("test-junit"))
}