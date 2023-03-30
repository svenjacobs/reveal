plugins {
	alias(libs.plugins.android.library)
	alias(libs.plugins.jetbrains.kotlin.android)
	`maven-publish`
	signing
}

android {
	namespace = "com.svenjacobs.reveal"
	compileSdk = Android.compileSdk

	defaultConfig {
		minSdk = Android.minSdk
		targetSdk = Android.targetSdk

		aarMetadata {
			minCompileSdk = Android.minSdk
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

	kotlinOptions {
		jvmTarget = "11"
		freeCompilerArgs += "-Xexplicit-api=strict"
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
	val composeBom = platform(libs.androidx.compose.bom)

	implementation(composeBom)
	api(libs.androidx.compose.foundation)
	api(libs.androidx.compose.animation)
	api(libs.androidx.compose.ui)

	debugApi(libs.androidx.compose.ui.tooling)
	debugApi(libs.androidx.compose.ui.test.manifest)

	testImplementation(libs.junit)
	androidTestImplementation(composeBom)
	androidTestImplementation(libs.androidx.test.ext.junit)
	androidTestImplementation(libs.androidx.test.espresso.core)
	androidTestImplementation(libs.androidx.compose.ui.test.junit4)
	androidTestImplementation(libs.androidx.compose.material3)

	lintChecks(libs.slack.compose.lint.checks)
}

publishing {
	publications {
		register<MavenPublication>("release") {
			groupId = Publication.group
			version = Publication.version
			artifactId = "reveal-core"

			afterEvaluate {
				from(components["release"])
			}

			pomAttributes(name = "Reveal (Core)")
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
