@file:OptIn(org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("multiplatform")
    id("com.android.kotlin.multiplatform.library")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

kotlin {
    applyDefaultHierarchyTemplate()

    jvm("desktop")

    android {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach {
        afterEvaluate {
            val baseName = extra.get("baseName") as String

            it.binaries.framework {
                this.baseName = baseName
            }
        }
    }

    js {
        browser()
    }

    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

    explicitApi()
}

// Since Compose Multiplatform 1.12, browser test tasks depend on a check that fails when the test
// classpath contains Skiko but the target declares no executable binary (CMP-4906). These library
// modules have no Compose UI browser tests and should not build executables, so skip the check.
tasks.matching { it.name.startsWith("checkComposeUiTestConfigurationFor") }.configureEach {
    enabled = false
}
