plugins {
	alias(libs.plugins.android.multiplatform.library)
	id("convention.multiplatform")
	id("convention.publication")
}

val baseName by extra { "reveal-shapes" }
val publicationName by extra { "Reveal (Shapes)" }

val androidMinSdk: Int by rootProject.extra
val androidCompileSdk: Int by rootProject.extra

kotlin {
	android {
		namespace = "com.svenjacobs.reveal.shapes"
		compileSdk { version = release(androidCompileSdk) }
		minSdk { version = release(androidMinSdk) }

		aarMetadata {
			minCompileSdk = androidMinSdk
		}
	}

	sourceSets {
		commonMain.dependencies {
			implementation(libs.compose.multiplatform.runtime)
			implementation(libs.compose.multiplatform.foundation)
		}
		commonTest.dependencies {
			implementation(kotlin("test"))
		}
	}
}

dependencies {
	lintChecks(libs.slack.compose.lint.checks)
}
