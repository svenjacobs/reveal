plugins {
	id("convention.multiplatform")
	id("convention.publication")
	alias(libs.plugins.android.library)
}

val baseName by extra { "reveal-common" }
val publicationName by extra { "Reveal (Common)" }

kotlin {
	sourceSets {
		val commonMain by getting {
			dependencies {
				implementation(compose.runtime)
				implementation(compose.foundation)
			}
		}
		val commonTest by getting {
			dependencies {
				implementation(kotlin("test"))
			}
		}
	}

	explicitApi()
}

val androidMinSdk: Int by rootProject.extra
val androidCompileSdk: Int by rootProject.extra

android {
	namespace = "com.svenjacobs.reveal.common"
	compileSdk = androidCompileSdk

	defaultConfig {
		minSdk = androidMinSdk

		aarMetadata {
			minCompileSdk = androidMinSdk
		}

		testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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

	buildFeatures {
		compose = true
	}

	composeOptions {
		kotlinCompilerExtensionVersion = libs.versions.androidx.compose.compiler.get()
	}
}

dependencies {
	lintChecks(libs.slack.compose.lint.checks)
}
