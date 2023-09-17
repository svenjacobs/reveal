import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
	alias(libs.plugins.jetbrains.kotlin.multiplatform)
	alias(libs.plugins.jetbrains.compose)
	alias(libs.plugins.android.library)
	id("convention.publication")
}

val publicationName by extra { "Reveal (Core)" }

@OptIn(ExperimentalKotlinGradlePluginApi::class)
kotlin {
	targetHierarchy.default()

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
			baseName = "reveal-core"
		}
	}

	sourceSets {
		val commonMain by getting {
			dependencies {
				api(project(":reveal-common"))
				implementation(compose.runtime)
				implementation(compose.foundation)
			}
		}
		val commonTest by getting {
			dependencies {
				implementation(kotlin("test"))
			}
		}
		val androidInstrumentedTest by getting {
			dependencies {
				implementation(project.dependencies.platform(libs.androidx.compose.bom))
				implementation(libs.androidx.test.ext.junit)
				implementation(libs.androidx.test.espresso.core)
				implementation(libs.androidx.compose.ui.test.junit4)
				implementation(libs.androidx.compose.material3)
			}
		}
	}

	explicitApi()
}

val androidMinSdk: Int by rootProject.extra
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

	buildFeatures {
		compose = true
	}

	composeOptions {
		kotlinCompilerExtensionVersion = libs.versions.androidx.compose.compiler.get()
	}

	publishing {
		singleVariant("release") {
			withSourcesJar()
			withJavadocJar()
		}
	}
}

dependencies {
	lintChecks(libs.slack.compose.lint.checks)
}
