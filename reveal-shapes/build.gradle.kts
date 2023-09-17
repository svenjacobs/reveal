import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
	alias(libs.plugins.jetbrains.kotlin.multiplatform)
	alias(libs.plugins.jetbrains.compose)
	alias(libs.plugins.android.library)
	`maven-publish`
	signing
}

@OptIn(ExperimentalKotlinGradlePluginApi::class)
kotlin {
	targetHierarchy.default()

	androidTarget {
		compilations.all {
			kotlinOptions {
				jvmTarget = "11"
			}
		}
	}

	listOf(
		iosX64(),
		iosArm64(),
		iosSimulatorArm64(),
	).forEach {
		it.binaries.framework {
			baseName = "reveal-shapes"
		}
	}

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

android {
	namespace = "com.svenjacobs.reveal.shapes"
	compileSdk = Android.compileSdk

	defaultConfig {
		minSdk = Android.minSdk

		aarMetadata {
			minCompileSdk = Android.minSdk
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

publishing {
	publications {
		register<MavenPublication>("release") {
			groupId = Publication.group
			version = Publication.version
			artifactId = "reveal-shapes"

			afterEvaluate {
				from(components["release"])
			}

			pomAttributes(name = "Reveal (Shapes)")
		}
	}
}

signing {
	// Store key and password in environment variables
	// ORG_GRADLE_PROJECT_signingKey and ORG_GRADLE_PROJECT_signingPassword
	val signingKey: String? by project
	val signingPassword: String? by project
	useInMemoryPgpKeys(signingKey, signingPassword)

	sign(publishing.publications["release"])
}
