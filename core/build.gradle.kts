import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {

    jvm {
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }

    js(IR) {
        browser {
            //distribution { directory = file(centyllionWebroot) }
            //webpackTask { outputFileName = "centyllion.[contenthash].js" }
        }
        binaries.library()

        compilations.forEach {
            it.kotlinOptions {
                moduleKind = "amd"
            }
        }
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        moduleName = "netbiodyn-core"
        browser {} 
        binaries.library()
    }

    sourceSets {

        commonMain {
            dependencies {
                api(libs.serialization.json)
                api(libs.benasher44)
            }
        }

        commonTest {
            dependencies {
                implementation(libs.bundles.test)
            }
        }

        jvmTest {
            dependencies {
                implementation(libs.bundles.test.jvm)
            }
        }

        jsTest {
            dependencies {
                implementation(libs.bundles.test.js)
            }
        }
    }
}
