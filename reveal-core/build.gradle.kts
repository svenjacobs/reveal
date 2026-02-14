plugins {
	alias(libs.plugins.android.multiplatform.library)
	id("convention.multiplatform")
	id("convention.publication")
}

val baseName by extra { "reveal-core" }
val publicationName by extra { "Reveal (Core)" }

val androidMinSdk: Int by rootProject.extra
val androidTargetSdk: Int by rootProject.extra
val androidCompileSdk: Int by rootProject.extra

kotlin {
	android {
		namespace = "com.svenjacobs.reveal"
		compileSdk { version = release(androidCompileSdk) }
		minSdk { version = release(androidMinSdk) }

		aarMetadata {
			minCompileSdk = androidMinSdk
		}
	}

	sourceSets {
		commonMain.dependencies {
			api(project(":reveal-common"))
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
