import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile
import java.security.MessageDigest

val serialization_version: String by project
val clikt_version: String by project
val ktor_version: String by project
val kotlinx_html_version: String by project

plugins {
    kotlin("multiplatform") version "1.3.21"
    id("kotlinx-serialization") version "1.3.21"
}

repositories {
    jcenter()
    maven("https://kotlin.bintray.com/kotlinx")
}

group = "com.centyllion"
version = "0.0.1"

kotlin {

    sourceSets {
        // commonMain is required for reflection name resolution
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime-common:$serialization_version")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }

        // Default source set for JVM-specific sources and dependencies:
        jvm {
            compilations["main"].defaultSourceSet {
                dependencies {
                    implementation(kotlin("stdlib-jdk8"))
                    implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:$serialization_version")

                    implementation("com.github.ajalt:clikt:$clikt_version")

                    implementation("io.ktor:ktor-html-builder:$ktor_version")
                    implementation("io.ktor:ktor-client-apache:$ktor_version")
                    implementation("io.ktor:ktor-auth:$ktor_version")
                    implementation("io.ktor:ktor-auth-jwt:$ktor_version")
                    implementation("io.ktor:ktor-network-tls:$ktor_version")
                    implementation("io.ktor:ktor-server-netty:$ktor_version")

                    implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:$kotlinx_html_version")
                }
            }
            // JVM-specific tests and their dependencies:
            compilations["test"].defaultSourceSet {
                dependencies {
                    implementation(kotlin("test-junit"))
                }
            }
        }

        js {
            compilations["main"].defaultSourceSet {
                dependencies {
                    implementation(kotlin("stdlib-js"))
                    implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime-js:$serialization_version")
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
                    main = "noCall"
                    sourceMap = true
                    sourceMapEmbedSources = "inlining"
                }
            }
        }
    }
}

tasks {
    val jsDir = "$buildDir/assemble/js"
    val webRoot = rootProject.file("webroot")
    val mainFunction = "com.centyllion.client.index()"

    val compileKotlinJs by existing(Kotlin2JsCompile::class)

    val allJs by creating(Copy::class) {
        dependsOn(compileKotlinJs)
        group = "build"

        doFirst {
            delete(jsDir)
        }
        from(
            fileTree(compileKotlinJs.get().destinationDir).matching { include("*.js", "*.map") }
        )
        configurations["jsMainImplementation"].all {
            from(zipTree(it.absolutePath).matching { include("*.js", "*.map") })
            true
        }
        into(jsDir)

        doLast {

            val modules = mutableMapOf<String, String>()

            // Adds md5 sum in file name for cache purposes
            file(jsDir).listFiles().forEach {
                val base = it.nameWithoutExtension
                val bytes = MessageDigest.getInstance("MD5").digest(it.readBytes())
                val builder = StringBuilder()
                for (b in bytes) builder.append(String.format("%02x", b))
                val sum = builder.toString()

                val extension = it.extension
                if (extension == "js" && !name.contains(sum)) {
                    val newBase = "$base.$sum"
                    modules[base] = newBase
                    it.renameTo(file("${it.parent}/$newBase.$extension"))
                }
            }

            val moduleJoined = modules.map { (k, v) -> "'$k': 'centyllion/$v'" }.joinToString(",\n")

            // Constructs a requirejs.config
            val configFile = file("${jsDir}/requirejs.config.json")
            configFile.writeText(
                """requirejs.config({
                'baseUrl': 'js',
                paths: {
                    'apexcharts': 'apexcharts/apexcharts',

                    $moduleJoined
                    }
                })

                requirejs(['centyllion'], function(centyllion) {
                    centyllion.$mainFunction
                })""".trimIndent()
            )
        }
    }

    val syncJs by creating(Sync::class) {
        dependsOn(allJs)
        group = "build"
        from(jsDir)
        into("$webRoot/js/centyllion")
    }

    val syncAssets by creating(Sync::class) {
        dependsOn(allJs)
        group = "build"
        from("$buildDir/resources/main")
        into("$webRoot/assets/centyllion")
    }

    val jsMainClasses by existing
    jsMainClasses.get().dependsOn(syncJs, syncAssets)
}
