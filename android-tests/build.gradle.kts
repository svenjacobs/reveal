plugins {
	alias(libs.plugins.android.application)
	alias(libs.plugins.jetbrains.kotlin.android)
}

val androidMinSdk: Int by rootProject.extra
val androidTargetSdk: Int by rootProject.extra
val androidCompileSdk: Int by rootProject.extra

android {
	namespace = "com.svenjacobs.reveal.android.tests"
	compileSdk = androidCompileSdk

	defaultConfig {
		applicationId = "com.svenjacobs.reveal.android.tests"
		minSdk = androidMinSdk
		targetSdk = androidTargetSdk
		versionCode = 1
		versionName = "1.0"

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

	kotlinOptions {
		jvmTarget = "11"
	}

	buildFeatures {
		compose = true
	}

	composeOptions {
		kotlinCompilerExtensionVersion = libs.versions.androidx.compose.compiler.get()
	}
}

dependencies {
	implementation(project(":reveal-core"))

	val composeBom = platform(libs.androidx.compose.bom)
	implementation(composeBom)
	implementation(libs.androidx.compose.foundation)
	implementation(libs.androidx.compose.runtime)
	implementation(libs.androidx.compose.animation)
	implementation(libs.androidx.compose.ui)
	implementation(libs.androidx.compose.material3)

	debugImplementation(libs.androidx.compose.ui.test.manifest)

	testImplementation(libs.junit)
	androidTestImplementation(composeBom)
	androidTestImplementation(libs.androidx.test.ext.junit)
	androidTestImplementation(libs.androidx.test.espresso.core)
	androidTestImplementation(libs.androidx.compose.ui.test.junit4)
	androidTestImplementation(libs.androidx.compose.material3)

	lintChecks(libs.slack.compose.lint.checks)
}
