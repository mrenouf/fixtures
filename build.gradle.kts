@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.multiplatform.library)
}

group = "com.bitgrind.fixtures"
version = "1.0-SNAPSHOT"

kotlin {
    android {
        compileSdk { version = release(36) }
        namespace = "com.bitgrind.fixtures"
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }
    java {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    jvm()
    jvmToolchain(21)
    wasmJs {
        browser {
            testTask {
                useKarma {
                    useChromeHeadlessNoSandbox()
                }
            }
        }
        generateTypeScriptDefinitions()
        binaries.library()
        compilerOptions {
            target = "es2015"
        }
    }
    js {
        browser()
        useEsModules()
        generateTypeScriptDefinitions()
        binaries.library()
        compilerOptions {
            target = "es2015"
        }
    }
    linuxX64()
    sourceSets {
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}
