import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

val debug: String? by project
val d = debug?.toBoolean() ?: false

val ktorVersion: String = "1.6.2"
val cliktVersion: String = "3.4.0"
val fontAwesomeVersion: String = "1.0.0"

val markdownVersion: String = "0.2.4"

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
}

dependencies {
    api(project(":core"))

    implementation(compose.desktop.currentOs)

    implementation("br.com.devsrsouza.compose.icons.jetbrains","font-awesome", fontAwesomeVersion)
    @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
    implementation(compose.desktop.components.splitPane)

    implementation("io.ktor", "ktor-client-core", ktorVersion)
    implementation("io.ktor", "ktor-client-cio", ktorVersion)
    implementation("io.ktor", "ktor-client-serialization", ktorVersion)

    implementation("com.github.ajalt.clikt","clikt", cliktVersion)

    testImplementation(kotlin("test-common"))
    testImplementation(kotlin("test-annotations-common"))
    testImplementation(kotlin("test-junit"))
}

compose.desktop {
    application {
        mainClass = "com.centyllion.ui.CentyllionKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "Centyllion"
            packageVersion = "1.0.0"
        }
    }
}