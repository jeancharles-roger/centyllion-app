val serializationVersion: String = "1.2.2"
val coroutineVersion: String = "1.4.2"
val kotlinxHtmlVersion: String = "0.7.3"
val bulmaKotlinVersion: String = "0.5"
val babylonKotlinVersion: String = "0.5.2"
val data2vizVersion: String = "0.10.1"
val markdownVersion: String = "0.2.4"

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

kotlin {

    jvm {
        compilations["test"].defaultSourceSet {
            dependencies {
                implementation(kotlin("test-junit"))
            }
        }

    }

    js(IR) {
        browser {
            //distribution { directory = file(centyllionWebroot) }
            //webpackTask { outputFileName = "centyllion.[contenthash].js" }
        }
        binaries.library()

        compilations["main"].defaultSourceSet {
            dependencies {

            }
        }
        compilations["test"].defaultSourceSet {
            dependencies {
                implementation(kotlin("test-js"))
            }
        }
        compilations.forEach {
            it.kotlinOptions {
                moduleKind = "amd"
            }
        }
    }

    sourceSets {

        // commonMain is required for reflection name resolution
        val commonMain by getting {
            dependencies {
                api("io.github.murzagalin:multiplatform-expressions-evaluator:0.15.0")

                api("org.jetbrains.kotlinx:kotlinx-serialization-core:$serializationVersion")
                api("org.jetbrains.kotlinx:kotlinx-serialization-json:$serializationVersion")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
    }
}

java.sourceCompatibility = JavaVersion.VERSION_11
