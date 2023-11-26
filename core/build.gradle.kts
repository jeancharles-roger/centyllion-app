plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {

    jvm()

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

    sourceSets {

        commonMain {
            dependencies {
                api(libs.evaluator)
                api(libs.serialization.json)
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
