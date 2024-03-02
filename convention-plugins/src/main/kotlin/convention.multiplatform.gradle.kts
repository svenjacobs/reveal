import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
	kotlin("multiplatform")
	id("org.jetbrains.compose")
}

val baseName: String by extra

@OptIn(ExperimentalKotlinGradlePluginApi::class)
kotlin {
	applyDefaultHierarchyTemplate()

	jvm("desktop")

	androidTarget {
		compilations.all {
			kotlinOptions {
				jvmTarget = "11"
			}
		}
		publishLibraryVariants("release")
	}

	listOf(
		iosX64(),
		iosArm64(),
		iosSimulatorArm64(),
	).forEach {
		it.binaries.framework {
			this.baseName = baseName
		}
	}

	js(IR) {
		browser()
	}

	explicitApi()
}
