@file:OptIn(org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
	kotlin("multiplatform")
	id("org.jetbrains.compose")
	id("org.jetbrains.kotlin.plugin.compose")
}

val baseName: String by extra
val outerBaseName = baseName

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

kotlin {
	applyDefaultHierarchyTemplate()

	jvm("desktop")

	androidTarget {
		compilerOptions {
			jvmTarget.set(JvmTarget.JVM_11)
		}
		publishLibraryVariants("release")
	}

	listOf(
		iosX64(),
		iosArm64(),
		iosSimulatorArm64(),
	).forEach {
		it.binaries.framework {
			baseName = outerBaseName
		}
	}

	js(IR) {
		browser()
	}

	@OptIn(org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl::class)
	wasmJs {
		browser()
	}

	explicitApi()
}
