@file:Suppress("UNUSED_VARIABLE")

import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*

val grpUser: String by project
val grpToken: String by project

val debug: String? by project
val d = debug?.toBoolean() ?: false

val serializationVersion: String = "1.0.1"
val coroutineVersion: String = "1.4.2"
val cliktVersion: String = "3.1.0"
val logbackVersion: String = "1.2.3"
val ktorVersion: String = "1.5.0"
val kotlinxHtmlVersion: String = "0.7.3"
val bulmaKotlinVersion: String = "0.4.2"
val babylonKotlinVersion: String = "0.5"
val exposedVersion: String = "0.32.1"
val postgresqlVersion: String = "42.2.5"
val keycloakVersion: String = "8.0.2"

val data2vizVersion: String = "0.8.12"

plugins {
    kotlin("multiplatform") version "1.5.21"
    kotlin("plugin.serialization") version "1.5.21"
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

group = "com.centyllion"
version = "0.0.1"

val centyllionWebroot = file("$projectDir/webroot/js/centyllion")

kotlin {
    // Default source set for JVM-specific sources and dependencies:
    jvm {
        compilations["main"].defaultSourceSet {
            dependencies {
                implementation("com.github.ajalt.clikt:clikt:$cliktVersion")

                // needed by ktor-auth-jwt (strange since it was included at some time ...)
                implementation("com.google.guava:guava:27.1-jre")

                if (d) implementation("ch.qos.logback:logback-classic:$logbackVersion")

                implementation("io.ktor:ktor-html-builder:$ktorVersion")
                implementation("io.ktor:ktor-client-apache:$ktorVersion")
                implementation("io.ktor:ktor-auth:$ktorVersion")
                implementation("io.ktor:ktor-auth-jwt:$ktorVersion")
                implementation("io.ktor:ktor-network-tls:$ktorVersion")
                implementation("io.ktor:ktor-network-tls-certificates:$ktorVersion")
                implementation("io.ktor:ktor-server-netty:$ktorVersion")
                implementation("io.ktor:ktor-serialization:$ktorVersion")

                implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:$kotlinxHtmlVersion")

                // adds dependencies for postgres
                implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
                implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
                implementation("org.jetbrains.exposed:exposed-jodatime:$exposedVersion")
                implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
                implementation("org.postgresql:postgresql:$postgresqlVersion")
                implementation("com.zaxxer:HikariCP:3.3.1") // Connection pool

                // adds dependencies to manage keycloak users
                implementation("org.jboss.resteasy:resteasy-client:3.6.1.Final")
                implementation("org.jboss.resteasy:resteasy-jaxrs:3.6.1.Final")
                implementation("org.jboss.resteasy:resteasy-jackson2-provider:3.6.1.Final")
                implementation("org.jboss.resteasy:resteasy-multipart-provider:3.6.1.Final")
                implementation("org.keycloak:keycloak-admin-client:$keycloakVersion")
            }
        }
        // JVM-specific tests and their dependencies:
        compilations["test"].defaultSourceSet {
            dependencies {
                implementation("io.ktor:ktor-server-test-host:$ktorVersion")
                implementation(kotlin("test-junit"))
            }
        }

        compilations.all {
            kotlinOptions {
                jvmTarget = "11"
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

                implementation("io.data2viz.d2v:core-js:$data2vizVersion")
                implementation("io.data2viz.d2v:color-js:$data2vizVersion")
                implementation("io.data2viz.d2v:scale-js:$data2vizVersion")
                implementation("io.data2viz.d2v:viz-js:$data2vizVersion")
                implementation("io.data2viz.d2v:axis:$data2vizVersion")
                
                implementation(npm("babylonjs", "4.0.3", generateExternals = false))
                implementation(npm("babylonjs-loaders", "4.0.3", generateExternals = false))
                implementation(npm("babylonjs-materials", "4.0.3", generateExternals = false))

                implementation(npm("keycloak-js", "8.0.2", generateExternals = false))
                implementation(npm("markdown-it", "10.0.0", generateExternals = false))
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
        all {
            languageSettings.useExperimentalAnnotation("kotlin.Experimental")
        }

        // commonMain is required for reflection name resolution
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:$serializationVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$serializationVersion")
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
    val cssDir = "$buildDir/assemble/main/css"
    val webRoot = rootProject.file("webroot")
    val deploy = rootProject.file("deploy")

    val mainFunction = "index()"
    val centyllionUrl = "https://127.0.0.1:8443"
    //val centyllionUrl = "https://app.centyllion.com"
    val externalFunction = "external(\"$centyllionUrl\")"

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

    val allCss by register<Copy>("allCss") {
        group = "build"

        doFirst {
            delete(cssDir)
        }
        from(
            fileTree("src/css").matching { include("*.css") }
        )
        into(cssDir)

        doLast {
            val files = mutableListOf<String>()

            // Adds md5 sum in file name for cache purposes
            file(cssDir).listFiles()?.forEach {
                val base = it.nameWithoutExtension
                val bytes = MessageDigest.getInstance("MD5").digest(it.readBytes())
                val builder = StringBuilder()
                for (b in bytes) builder.append(String.format("%02x", b))
                val sum = builder.toString()

                val extension = it.extension
                if (extension == "css" && !name.contains(sum)) {
                    val newFile = "$base.$sum.$extension"
                    files.add(newFile)
                    it.renameTo(file("${it.parent}/$newFile"))
                }
            }

            // Constructs a css.files
            val cssFiles = file("$cssDir/css.config.json")
            val content = files.joinToString(",") { "\"/css/centyllion/$it\"" }
            cssFiles.writeText("{ \"files\": [$content] }")
        }
    }

    val jsBrowserWebpack by existing

    val syncJs by register("syncJs") {
        dependsOn(jsBrowserWebpack)
        group = "build"
        doLast {
            // find js distributed file
            val jsFile = project.buildDir.resolve("distributions/centyllion.js")
            val mapFile = project.buildDir.resolve("distributions/centyllion.js.map")

            // Adds md5 sum in file name for cache purposes
            val base = jsFile.nameWithoutExtension
            val bytes = MessageDigest.getInstance("MD5").digest(jsFile.readBytes())
            val builder = StringBuilder()
            for (b in bytes) builder.append(String.format("%02x", b))
            val sum = builder.toString()

            // prepare and clear destination
            centyllionWebroot.deleteRecursively()
            centyllionWebroot.mkdirs()

            jsFile.copyTo(centyllionWebroot.resolve("$base.$sum.js"))
            mapFile.copyTo(centyllionWebroot.resolve("$base.$sum.js.map"))
        }
    }

    val syncCss by register<Sync>("syncCss") {
        dependsOn(allCss)
        group = "build"
        from(cssDir)
        into("$webRoot/css/centyllion")
    }

    val syncAssets by register<Sync>("syncAssets") {
        dependsOn(generateVersion)
        group = "build"
        from("$buildDir/resources/main")
        into("$webRoot/assets/centyllion")
    }

    val jsJar by existing
    jsJar.get().dependsOn(syncJs, syncCss, syncAssets)

    val assemble by existing
    val jvmJar by existing(Jar::class)
    val distribution = register<Tar>("distribution") {
        group = "deployment"
        dependsOn(assemble)

        // web assets and javascript
        from(webRoot) { into("webroot") }

        // start/stop scripts
        from(deploy)

        // jars for server
        from(project.configurations["jvmDefault"].resolve()) { into("libs") }
        from(jvmJar.get().archiveFile.get()) { into("libs") }

        compression = Compression.GZIP
    }
}
