@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
	alias(libs.plugins.jetbrains.kotlin.multiplatform)
	alias(libs.plugins.android.library)
	alias(libs.plugins.jetbrains.compose)
	alias(libs.plugins.compose.compiler)
}

kotlin {
	jvmToolchain(17)

	applyDefaultHierarchyTemplate()

	jvm("desktop")

	androidTarget {
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
			implementation(compose.runtime)
			implementation(compose.foundation)
			implementation(compose.material3)
			implementation(compose.materialIconsExtended)
			@OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
			implementation(compose.components.resources)

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

android {
	namespace = "com.svenjacobs.reveal.demo"
	compileSdk = 36
	defaultConfig {
		minSdk = 23
	}
}
