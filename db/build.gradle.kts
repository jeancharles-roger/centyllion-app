plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.sqldelight)
    alias(libs.plugins.graalvm.native.image)
}

dependencies {
    api(libs.serialization.json)

    implementation(libs.clikt)
    implementation(libs.postgresql)
    implementation(libs.sqldelight.jdbc)

    testImplementation(libs.bundles.test)
    testImplementation(libs.bundles.test.jvm)
}

sqldelight {
    databases {
        create("Database") {
            packageName.set("com.centyllion.db")
            dialect("app.cash.sqldelight:postgresql-dialect:2.0.0")

        }
    }
}

nativeImage {
    val property = providers.gradleProperty("graalVmHome").orNull
    val envGraalHome = System.getenv("GRAALVM_HOME")
    graalVmHome = when {
        envGraalHome != null -> {
            System.err.println("Env variable 'GRAALVM_HOME' is set, using it")
            envGraalHome
        }
        property == null -> {
            System.err.println("Property 'graalVmHome' isn't set in gradle.properties, using JAVA_HOME")
            System.getenv("JAVA_HOME")
        }
        property.isBlank() -> {
            System.err.println("Property 'graalVmHome' is blank in gradle.properties, using JAVA_HOME")
            System.getenv("JAVA_HOME")
        }
        !file(property).exists() -> {
            System.err.println("Path '$property' from 'graalVmHome' in gradle.properties doesn't exist, using JAVA_HOME")
            System.getenv("JAVA_HOME")
        }
        !file(property).isDirectory -> {
            System.err.println("Path '$property' from 'graalVmHome' in gradle.properties isn't a directory, using JAVA_HOME")
            System.getenv("JAVA_HOME")
        }
        else -> property
    }

    buildType { build ->
        build.executable(main = "com.centyllion.db.MainKt")
    }

    //jarTask = getTasksByName("jar", false).first() as Jar

    executableName = "centyllion-db"
    runtimeClasspath = configurations["runtimeClasspath"]
    arguments("--no-fallback")
}