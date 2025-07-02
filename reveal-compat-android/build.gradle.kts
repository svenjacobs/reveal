import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
	alias(libs.plugins.android.library)
	alias(libs.plugins.jetbrains.kotlin.android)
	alias(libs.plugins.compose.compiler)
	`maven-publish`
	id("convention.publication")
}

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

val publicationName by extra { "Reveal (Compat Android)" }

val androidMinSdk: Int by rootProject.extra
val androidCompileSdk: Int by rootProject.extra

android {
	namespace = "com.svenjacobs.reveal.compat.android"
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

	publishing {
		singleVariant("release") {
			withSourcesJar()
		}
	}

	lint {
		baseline = file("lint-baseline.xml")
	}
}

kotlin {
	explicitApi()
	compilerOptions {
		jvmTarget = JvmTarget.JVM_11
	}
}

publishing {
	publications {
		register<MavenPublication>("release") {
			afterEvaluate {
				from(components["release"])
			}
		}
	}
}

dependencies {
	api(project(":reveal-common"))

	val composeBom = platform(libs.androidx.compose.bom)

	implementation(composeBom)
	api(libs.androidx.compose.foundation)
	api(libs.androidx.compose.ui)

	debugApi(libs.androidx.compose.ui.tooling)

	testImplementation(libs.junit)
	androidTestImplementation(composeBom)
	androidTestImplementation(libs.androidx.test.ext.junit)
	androidTestImplementation(libs.androidx.test.espresso.core)
	androidTestImplementation(libs.androidx.compose.ui.test.junit4)

	lintChecks(libs.slack.compose.lint.checks)
}
