plugins {
	alias(libs.plugins.android.library)
	id("convention.multiplatform")
	id("convention.publication")
}

val baseName by extra { "reveal-core" }
val publicationName by extra { "Reveal (Core)" }

kotlin {
	sourceSets {
		commonMain.dependencies {
			api(project(":reveal-common"))
			implementation(compose.runtime)
			implementation(compose.foundation)
		}
		commonTest.dependencies {
			implementation(kotlin("test"))
		}
	}
}

val androidMinSdk: Int by rootProject.extra
val androidTargetSdk: Int by rootProject.extra
val androidCompileSdk: Int by rootProject.extra

android {
	namespace = "com.svenjacobs.reveal"
	compileSdk = androidCompileSdk

	defaultConfig {
		minSdk = androidMinSdk

		aarMetadata {
			minCompileSdk = androidMinSdk
		}

		testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
		consumerProguardFiles("consumer-rules.pro")
	}

	buildTypes {
		release {
			isMinifyEnabled = false
			proguardFiles(
				getDefaultProguardFile("proguard-android-optimize.txt"),
				"proguard-rules.pro",
			)
		}
	}

	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_11
		targetCompatibility = JavaVersion.VERSION_11
	}
}

dependencies {
	lintChecks(libs.slack.compose.lint.checks)
}
