@file:Suppress("UNUSED_VARIABLE")

import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*

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
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

repositories {
    mavenCentral()

    maven {
        url = uri("https://maven.pkg.github.com/centyllion/bulma-kotlin")
        credentials {
            username = grpUser
            password = grpToken
        }
    }
    maven {
        url = uri("https://maven.pkg.github.com/centyllion/babylon-kotlin")
        credentials {
            username = grpUser
            password = grpToken
        }
    }

    maven { url = uri("https://maven.pkg.jetbrains.space/data2viz/p/maven/public") }
    maven { url = uri("https://maven.pkg.jetbrains.space/public/p/kotlinx-html/maven") }
}

val centyllionWebroot = file("$projectDir/webroot/js/centyllion")

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
        binaries.executable()

        compilations["main"].defaultSourceSet {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-html-js:$kotlinxHtmlVersion")

                implementation("com.centyllion:bulma-kotlin:$bulmaKotlinVersion")
                implementation("com.centyllion:babylon-kotlin:$babylonKotlinVersion")

                implementation("io.data2viz.d2v:d2v-core:$data2vizVersion")
                implementation("io.data2viz.d2v:d2v-color:$data2vizVersion")
                implementation("io.data2viz.d2v:d2v-scale:$data2vizVersion")
                implementation("io.data2viz.d2v:d2v-viz:$data2vizVersion")
                implementation("io.data2viz.d2v:d2v-axis:$data2vizVersion")

                implementation("org.jetbrains:markdown:$markdownVersion")

                implementation(npm("babylonjs", "4.0.3", generateExternals = false))
                implementation(npm("babylonjs-loaders", "4.0.3", generateExternals = false))
                implementation(npm("babylonjs-materials", "4.0.3", generateExternals = false))

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
                //main = "noCall"
                sourceMap = d
                sourceMapEmbedSources = if (d) "always" else "never"
            }
        }
    }

    sourceSets {

        // commonMain is required for reflection name resolution
        val commonMain by getting {
            dependencies {
                implementation(project(":core"))
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

tasks {
    val jsDir = "$buildDir/assemble/main/js"
    val webRoot = file("$projectDir/webroot")

    val mainFunction = "index()"

    val compileKotlinJs by existing(Kotlin2JsCompile::class)

    val generateVersion by register("generateVersion") {
        doLast {
            val properties = project.ext.properties
            val version =  project.version
            val build = properties["build.number"] ?: "dev"
            val sha = properties["build.vcs.number"] ?: "dev"
            val date = SimpleDateFormat().format(Date())
            file("$webRoot/version.json").writeText(
                """{ "version": "$version", "build": "$build", "sha": "$sha", "date": "$date" }"""
            )
        }
    }

    val jsBrowserWebpack by existing

    val syncJs by register("syncJs") {
        dependsOn(jsBrowserWebpack)
        dependsOn(generateVersion)
        group = "build"
        doLast {
            // find js distributed file
            val jsFile = project.buildDir.resolve("dist/js/productionExecutable/web.js")
            val mapFile = project.buildDir.resolve("dist/js/productionExecutable/web.js.map")

            // Adds md5 sum in file name for cache purposes
            val base = "centyllion"
            val bytes = MessageDigest.getInstance("MD5").digest(jsFile.readBytes())
            val builder = StringBuilder()
            for (b in bytes) builder.append(String.format("%02x", b))
            val sum = builder.toString()

            // prepare and clear destination
            centyllionWebroot.deleteRecursively()
            centyllionWebroot.mkdirs()

            //jsFile.copyTo(centyllionWebroot.resolve("$base.$sum.js"))
            //mapFile.copyTo(centyllionWebroot.resolve("$base.$sum.js.map"))
            jsFile.copyTo(centyllionWebroot.resolve("$base.js"))
            mapFile.copyTo(centyllionWebroot.resolve("$base.js.map"))
        }
    }

    val jsJar by existing
    jsJar.get().dependsOn(syncJs)
}
