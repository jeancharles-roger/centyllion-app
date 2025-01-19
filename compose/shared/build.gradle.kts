import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.jetbrains)
    alias(libs.plugins.compose.compiler)
}

kotlin {

    jvm()

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        moduleName = "netbiodyn"
        browser {
            val rootDirPath = project.rootDir.path
            val projectDirPath = project.projectDir.path
            commonWebpackConfig {
                outputFileName = "netbiodyn.js"
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                    static = (static ?: mutableListOf()).apply {
                        // Serve sources to debug inside browser
                        add(rootDirPath)
                        add(projectDirPath)
                    }
                }
            }
        }
        binaries.executable()
    }

    sourceSets {
        commonMain {
            dependencies {
                api(project(":core"))

                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material)
                implementation(compose.materialIconsExtended)
                implementation(compose.ui)
                implementation(compose.components.resources)
                implementation(compose.components.uiToolingPreview)

                //implementation(libs.markdown)
                implementation(libs.fontawesome)
                implementation(libs.filekit)
            }
        }

        commonTest {
            dependencies {
                implementation(libs.bundles.test)
            }
        }

        jvmMain {
            dependencies {
                api(project(":core"))

                implementation(compose.desktop.currentOs)
                //implementation(libs.clikt)
            }
        }

        jvmTest {
            dependencies {
                implementation(libs.bundles.test.jvm)
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "com.centyllion.ui.NetbiodynKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "Centyllion"
            packageVersion = "1.0.0"
        }
    }
}
