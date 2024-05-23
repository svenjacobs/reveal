plugins {
	kotlin("multiplatform")
	id("com.android.library")
	id("org.jetbrains.compose")
}

kotlin {
	jvmToolchain(17)

	applyDefaultHierarchyTemplate()

	jvm("desktop")

	androidTarget {
		compilations.all {
			kotlinOptions {
				jvmTarget = "17"
			}
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
			val revealVersion = "3.0.5"

			implementation(compose.runtime)
			implementation(compose.foundation)
			implementation(compose.material3)
			@OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
			implementation(compose.components.resources)

			implementation("com.svenjacobs.reveal:reveal-core:$revealVersion")
			implementation("com.svenjacobs.reveal:reveal-shapes:$revealVersion")
		}

		commonTest.dependencies {
			implementation(kotlin("test"))
		}
	}
}

android {
	namespace = "com.svenjacobs.reveal.demo"
	compileSdk = 34
	defaultConfig {
		minSdk = 21
	}
}
