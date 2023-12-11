import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose)
}

kotlin {
    jvm()

    sourceSets {
        commonMain {
            dependencies {
                api(project(":core"))

                // Note, if you develop a library, you should use compose.desktop.common.
                // compose.desktop.currentOs should be used in launcher-sourceSet
                // (in a separate module for demo project and in testMain).
                // With compose.desktop.common you will also lose @Preview functionality
                implementation(compose.desktop.currentOs)
                implementation(compose.materialIconsExtended)

                implementation(libs.markdown)
                implementation(libs.font.awesome)
                implementation(libs.clikt)

                @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
                implementation(compose.desktop.components.splitPane)
            }
        }
        commonTest {
            dependencies {
                implementation(libs.bundles.test)
                implementation(libs.bundles.test.jvm)
            }
        }
    }
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
