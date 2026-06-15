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
			val baseName: String by extra

			it.binaries.framework {
				this.baseName = baseName
			}
		}
	}

	js(IR) {
		browser()
	}

	@OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
	wasmJs {
		browser()
	}

	explicitApi()
}
