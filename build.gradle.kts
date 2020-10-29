@file:Suppress("UNUSED_VARIABLE")

val debug: String? by project
val d = debug?.toBoolean() ?: false

val serialization_version: String = "1.0.0"
val coroutine_version: String = "1.3.9"
val clikt_version: String = "2.8.0"
val logback_version: String = "1.2.3"
val ktor_version: String = "1.4.0"
val kotlinx_html_version: String = "0.7.1"
val bulma_kotlin_version: String = "0.4"
val babylon_kotlin_version: String = "0.4"
val exposed_version: String = "0.17.5"
val postgresql_version: String = "42.2.5"
val keycloak_version: String = "8.0.2"

plugins {
    kotlin("multiplatform") version "1.4.10"
    id("kotlinx-serialization") version "1.4.10"
}

repositories {
    jcenter()
    mavenCentral()
    maven("https://kotlin.bintray.com/kotlinx")
    maven("https://jitpack.io")
    maven("https://dl.bintray.com/centyllion/Libraries")
}

group = "com.centyllion"
version = "0.0.1"

kotlin {
    
    sourceSets {
        all {
            languageSettings.useExperimentalAnnotation("kotlin.Experimental")
        }

        // commonMain is required for reflection name resolution
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:$serialization_version")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$serialization_version")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
    }

    // Default source set for JVM-specific sources and dependencies:
    jvm {
        compilations["main"].defaultSourceSet {
            dependencies {
                implementation("com.github.ajalt:clikt:$clikt_version")

                // needed by ktor-auth-jwt (strange since it was included at some time ...)
                implementation("com.google.guava:guava:27.1-jre")

                if (d) implementation("ch.qos.logback:logback-classic:$logback_version")

                implementation("io.ktor:ktor-html-builder:$ktor_version")
                implementation("io.ktor:ktor-client-apache:$ktor_version")
                implementation("io.ktor:ktor-auth:$ktor_version")
                implementation("io.ktor:ktor-auth-jwt:$ktor_version")
                implementation("io.ktor:ktor-network-tls:$ktor_version")
                implementation("io.ktor:ktor-network-tls-certificates:$ktor_version")
                implementation("io.ktor:ktor-server-netty:$ktor_version")
                implementation("io.ktor:ktor-serialization:$ktor_version")

                implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:$kotlinx_html_version")

                // adds dependencies for postgres
                implementation("org.jetbrains.exposed:exposed:$exposed_version")
                implementation("org.postgresql:postgresql:$postgresql_version")
                implementation("com.zaxxer:HikariCP:3.3.1") // Connection pool

                // adds dependencies to manage keycloak users
                implementation("org.jboss.resteasy:resteasy-client:3.6.1.Final")
                implementation("org.jboss.resteasy:resteasy-jaxrs:3.6.1.Final")
                implementation("org.jboss.resteasy:resteasy-jackson2-provider:3.6.1.Final")
                implementation("org.jboss.resteasy:resteasy-multipart-provider:3.6.1.Final")
                implementation("org.keycloak:keycloak-admin-client:$keycloak_version")
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
                jvmTarget = "11"
            }
        }
    }

    js {
        browser {
            distribution { directory = file("$projectDir/webroot/js/centyllion") }
            /*
            webpackTask {
               outputFileName = "tekdiving-shop.[contenthash].js"
            }
             */
        }
        binaries.executable()

        compilations["main"].defaultSourceSet {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-html-js:$kotlinx_html_version")

                implementation("com.centyllion:bulma-kotlin:$bulma_kotlin_version")
                implementation("com.centyllion:babylon-kotlin:$babylon_kotlin_version")

                implementation(npm("babylonjs", "4.0.3", generateExternals = false))
                implementation(npm("babylonjs-loaders", "4.0.3", generateExternals = false))
                implementation(npm("babylonjs-materials", "4.0.3", generateExternals = false))

                implementation(npm("uplot", "1.2.2", generateExternals = false))
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
}

java.sourceCompatibility = JavaVersion.VERSION_11

/*
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

    val allJs by register<Copy>("allJs") {
        // adds require template file to task input
        val requireTemplate = file("deploy/requirejs.config.template")
        inputs.file(requireTemplate)
        dependsOn(compileKotlinJs)
        group = "build"

        doFirst { delete(jsDir) }

        // includes sources js
        from(fileTree(compileKotlinJs.get().destinationDir).matching { include("*.js", "*.map") })
        val t = compileKotlinJs.get()
        // includes dependencies js
        val configuration = "jsMainImplementationDependenciesMetadata"
        configurations[configuration].forEach {
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

            val moduleJoined = modules.map { (k, v) -> "'$k': 'centyllion/$v'" }.joinToString(",\n\t\t")

            fun copyRequireJsConfig(name: String, baseUrl: String, main: String) {
                val sourceFile = requireTemplate
                val content = sourceFile.readText()
                    .replace("%DEPENDENCIES%", moduleJoined)
                    .replace("%BASEURL%", baseUrl)
                    .replace("%MAIN%", main)
                file(name).writeText(content)
            }

            copyRequireJsConfig("${jsDir}/requirejs.config.json", "/js", mainFunction)
            copyRequireJsConfig("${jsDir}/centyllion.config.json", "https://127.0.0.1:8443/js", externalFunction)

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
}
*/