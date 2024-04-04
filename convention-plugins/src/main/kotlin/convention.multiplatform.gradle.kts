plugins {
	kotlin("multiplatform")
	id("org.jetbrains.compose")
}

val baseName: String by extra

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

	@OptIn(org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl::class)
	wasmJs {
		browser()
	}

	explicitApi()
}
