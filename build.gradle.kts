@file:Suppress("UNUSED_VARIABLE")

import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.URLProtocol
import io.ktor.http.content.TextContent
import io.ktor.http.toHttpDate
import io.ktor.util.date.GMTDate
import kotlinx.coroutines.runBlocking
import org.apache.commons.codec.binary.Base64
import org.gradle.jvm.tasks.Jar
import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile
import java.security.MessageDigest

val debug: String by project

val serialization_version: String by project
val coroutine_version: String by project
val clikt_version: String by project
val ktor_version: String by project
val kotlinx_html_version: String by project
val exposed_version: String by project
val postgresql_version: String by project
val keycloak_version: String by project
val stripe_version: String by project

buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        classpath("io.ktor:ktor-client-apache:1.1.3")
    }
}

plugins {
    kotlin("multiplatform") version "1.3.41"
    id("kotlinx-serialization") version "1.3.41"
}

repositories {
    jcenter()
    maven("https://kotlin.bintray.com/kotlinx")
    maven("https://jitpack.io")
    mavenCentral()
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

                    // needed by ktor-auth-jwt (strange since it was included at some time ...)
                    implementation("com.google.guava:guava:27.1-jre")

                    implementation("io.ktor:ktor-html-builder:$ktor_version")
                    implementation("io.ktor:ktor-client-apache:$ktor_version")
                    implementation("io.ktor:ktor-auth:$ktor_version")
                    implementation("io.ktor:ktor-auth-jwt:$ktor_version")
                    implementation("io.ktor:ktor-network-tls:$ktor_version")
                    implementation("io.ktor:ktor-server-netty:$ktor_version")

                    implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:$kotlinx_html_version")

                    // adds dependences for postgres
                    implementation("org.jetbrains.exposed:exposed:$exposed_version")
                    implementation("org.postgresql:postgresql:$postgresql_version")
                    implementation("com.zaxxer:HikariCP:3.3.1") // Connection pool

                    // adds dependencies to manage keycloak users
                    implementation("org.jboss.resteasy:resteasy-client:3.6.1.Final")
                    implementation("org.jboss.resteasy:resteasy-jaxrs:3.6.1.Final")
                    implementation("org.jboss.resteasy:resteasy-jackson2-provider:3.6.1.Final")
                    implementation("org.jboss.resteasy:resteasy-multipart-provider:3.6.1.Final")
                    implementation("org.keycloak:keycloak-admin-client:$keycloak_version")
                    
                    // ads dependencies for stripe
                    implementation("com.stripe:stripe-java:$stripe_version")
                }
            }
            // JVM-specific tests and their dependencies:
            compilations["test"].defaultSourceSet {
                dependencies {
                    implementation("io.ktor:ktor-server-test-host:$ktor_version")
                    implementation(kotlin("test-junit"))
                }
            }

            compilations.forEach {
                it.kotlinOptions {
                    jvmTarget = "1.8"

                }
            }
        }

        js {
            compilations["main"].defaultSourceSet {
                dependencies {
                    implementation(kotlin("stdlib-js"))
                    implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime-js:$serialization_version")
                    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:$coroutine_version")
                    implementation("org.jetbrains.kotlinx:kotlinx-html-js:$kotlinx_html_version")

                    implementation("com.github.markaren:three.kt:v0.88-ALPHA-7")
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
                    sourceMap = debug.toBoolean()
                    sourceMapEmbedSources = if (debug.toBoolean()) "always" else "never"
                }
            }
        }
    }
}

java.sourceCompatibility = JavaVersion.VERSION_1_8

tasks {
    val jsDir = "$buildDir/assemble/main/js"
    val cssDir = "$buildDir/assemble/main/css"
    val webRoot = rootProject.file("webroot")
    val deploy = rootProject.file("deploy")

    val mainFunction = "com.centyllion.client.index()"
    val centyllionUrl = "https://beta.centyllion.com"
    val externalFunction = "com.centyllion.client.external(\"$centyllionUrl\")"

    val compileKotlinJs by existing(Kotlin2JsCompile::class)

    val generateVersion by register("generateVersion") {
        doLast {
            val version =  System.getenv("GITHUB_REF") ?: "localrepository"
            val build = System.getenv("GITHUB_SHA") ?: "dev"
            val date = GMTDate().toHttpDate()
            file("$webRoot/version.json").writeText(
                """{ "version": "$version", "build": "$build", "date": "$date" }"""
            )
        }
    }

    val allJs by register<Copy>("allJs") {
        dependsOn(compileKotlinJs)
        group = "build"

        doFirst {
            delete(jsDir)
        }
        from(
            fileTree(compileKotlinJs.get().destinationDir).matching { include("*.js", "*.map") }
        )

        configurations["jsMainImplementationDependenciesMetadata"].forEach {
            from(zipTree(it.absolutePath).matching { include("*.js", "*.map") })
        }

        into(jsDir)

        doLast {
            val modules = mutableMapOf<String, String>()

            // Adds md5 sum in file name for cache purposes
            file(jsDir).listFiles()?.forEach {
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
                        'chartjs': 'Chart.js-2.8.0/Chart',
                        'bulmaToast': 'bulma-toast-1.5.0/bulma-toast.min',
                        $moduleJoined
                    }
                })
                
                requirejs(['chartjs', 'bulmaToast', 'centyllion'], function(chartjs, bulmaToast, centyllion) {
                    centyllion.com.centyllion.client.dependencies(bulmaToast)
                    centyllion.$mainFunction
                })""".trimIndent()
            )

            // Constructs a centyllion.config.json
            val centyllionFile = file("$jsDir/centyllion.config.json")
            centyllionFile.writeText(
                """requirejs.config({
                    'baseUrl': '$centyllionUrl/js',
                    paths: {
                        'chartjs': 'Chart.js-2.8.0/Chart',
                        'bulmaToast': 'bulma-toast-1.5.0/bulma-toast.min',
                        $moduleJoined
                    }
                })
                
                requirejs(['chartjs', 'bulmaToast', 'centyllion'], function(chartjs, bulmaToast, centyllion) {
                    centyllion.com.centyllion.client.dependencies(bulmaToast)
                    centyllion.$externalFunction
                })""".trimIndent()
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
            val content = files.map { "\"css/centyllion/$it\"" }.joinToString(",")
            cssFiles.writeText("{ \"files\": [$content] }")
        }
    }

    val syncJs by register<Sync>("syncJs") {
        dependsOn(allJs)
        group = "build"
        from(jsDir)
        into("$webRoot/js/centyllion")
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

    register("deployBeta") {
        group = "deployment"
        dependsOn(distribution)

        doLast {
            runBlocking {
                val client = HttpClient(Apache)

                val request = HttpRequestBuilder().apply {
                    method = HttpMethod.Post
                    url.protocol = URLProtocol.HTTPS
                    url.host = "deploy.centyllion.com"
                    url.path("hooks","deploy-beta")

                    val password = System.getenv("PASSWORD")?: System.getProperty("password")
                    if (password != null) header("X-Password", password)

                    val key = System.getenv("DEPLOY_KEY") ?: System.getProperty("deploy.key")
                    header("X-Token", key)

                    val bytes = distribution.get().archiveFile.get().asFile.readBytes()
                    val payload = """{ "content": "${Base64.encodeBase64String(bytes)}" }"""
                    body = TextContent(payload, ContentType.Application.Json)
                }

                val result = client.post<String>(request)
                client.close()

                if (result.contains("failed", true)) {
                    throw GradleException("Deploy failed due to: $result")
                }
            }
        }
    }

}
