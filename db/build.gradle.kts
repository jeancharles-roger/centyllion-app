plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.sqldelight)
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