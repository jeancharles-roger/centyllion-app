plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.sqldelight)
    application
}

dependencies {
    api(project(":core"))
    api(libs.serialization.json)

    implementation(libs.clikt)

    implementation(libs.postgresql)
    implementation(libs.sqldelight.jdbc)

    implementation(libs.bundles.ktor)

    testImplementation(libs.bundles.test)
    testImplementation(libs.bundles.test.jvm)
}

sqldelight {
    databases {
        create("Database") {
            packageName.set("com.centyllion.db")
            dialect("app.cash.sqldelight:postgresql-dialect:2.0.1")
        }
    }
}

application {
    mainClass = "com.centyllion.db.MainKt"
}

tasks {
    task<Copy>("copyForDocker") {
        group = "distribution"
        dependsOn("distTar")
        from("build/distributions") {
            include("${project.name}-${version}.tar")
        }
        into("docker")
    }

}