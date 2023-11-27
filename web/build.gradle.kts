@file:Suppress("UNUSED_VARIABLE")

import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*

val grpUser: String by project
val grpToken: String by project

val debug: String? by project
val d = debug?.toBoolean() ?: false

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
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

    js(IR) {
        browser {
            //distribution { directory = file(centyllionWebroot) }
            //webpackTask { outputFileName = "centyllion.[contenthash].js" }
        }
        binaries.executable()

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
        commonMain {
            dependencies {
                implementation(project(":core"))
                implementation(libs.kotlinx.html)
            }
        }

        commonTest {
            dependencies {
                implementation(libs.bundles.test)
            }
        }

        jsMain {
            dependencies {
                implementation(libs.bulma.kotlin)
                implementation(libs.babylon.kotlin)
                implementation(libs.bundles.data2viz)
                implementation(libs.markdown)

                /*
                implementation(npm("babylonjs", "4.0.3", generateExternals = false))
                implementation(npm("babylonjs-loaders", "4.0.3", generateExternals = false))
                implementation(npm("babylonjs-materials", "4.0.3", generateExternals = false))
                 */
            }
        }

        jsTest {
            dependencies {
                implementation(libs.bundles.test.js)
            }
        }
    }
}


tasks {
    val jsDir = "${project.layout.buildDirectory}/assemble/main/js"
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
            val jsFile = project.layout.buildDirectory.file("dist/js/productionExecutable/web.js")
            val mapFile = project.layout.buildDirectory.file("dist/js/productionExecutable/web.js.map")

            // Adds md5 sum in file name for cache purposes
            val base = "centyllion"
            val bytes = MessageDigest.getInstance("MD5").digest(jsFile.get().asFile.readBytes())
            val builder = StringBuilder()
            for (b in bytes) builder.append(String.format("%02x", b))
            val sum = builder.toString()

            // prepare and clear destination
            centyllionWebroot.deleteRecursively()
            centyllionWebroot.mkdirs()

            //jsFile.copyTo(centyllionWebroot.resolve("$base.$sum.js"))
            //mapFile.copyTo(centyllionWebroot.resolve("$base.$sum.js.map"))
            jsFile.get().asFile.copyTo(centyllionWebroot.resolve("$base.js"))
            mapFile.get().asFile.copyTo(centyllionWebroot.resolve("$base.js.map"))
        }
    }

    val jsJar by existing
    jsJar.get().dependsOn(syncJs)
}
