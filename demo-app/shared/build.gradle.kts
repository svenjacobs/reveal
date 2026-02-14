@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
	alias(libs.plugins.jetbrains.kotlin.multiplatform)
	alias(libs.plugins.android.multiplatform.library)
	alias(libs.plugins.jetbrains.compose)
	alias(libs.plugins.compose.compiler)
}

kotlin {
	jvmToolchain(17)

	applyDefaultHierarchyTemplate()

	jvm("desktop")

	android {
		namespace = "com.svenjacobs.reveal.demo"
		minSdk { version = release(23) }
		compileSdk { version = release(36) }
	}

	androidLibrary {
		compilerOptions {
			jvmTarget.set(JvmTarget.JVM_17)
		}
	}

	listOf(
		iosX64(),
		iosArm64(),
		iosSimulatorArm64(),
	).forEach {
		it.binaries.framework {
			baseName = "shared"
		}
	}

	sourceSets {
		commonMain.dependencies {
			implementation(libs.compose.multiplatform.runtime)
			implementation(libs.compose.multiplatform.foundation)
			implementation(libs.compose.multiplatform.material3)
			implementation(libs.compose.multiplatform.material.icons.extended)
			implementation(libs.compose.multiplatform.components.resources)

			//noinspection UseTomlInstead
			implementation("reveal:core")
			//noinspection UseTomlInstead
			implementation("reveal:shapes")
		}

		commonTest.dependencies {
			implementation(kotlin("test"))
		}
	}
}
