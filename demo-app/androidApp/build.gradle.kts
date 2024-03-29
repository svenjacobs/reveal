plugins {
	id("com.android.application")
	kotlin("android")
}

android {
	namespace = "com.svenjacobs.reveal.demo.android"
	compileSdk = 34
	defaultConfig {
		applicationId = "com.svenjacobs.reveal.demo.android"
		minSdk = 21
		targetSdk = 34
		versionCode = 1
		versionName = "1.0"
	}
	buildFeatures {
		compose = true
	}
	composeOptions {
		kotlinCompilerExtensionVersion = "1.5.10"
	}
	packaging {
		resources {
			excludes += "/META-INF/{AL2.0,LGPL2.1}"
		}
	}
	buildTypes {
		getByName("release") {
			isMinifyEnabled = false
		}
	}
	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_17
		targetCompatibility = JavaVersion.VERSION_17
	}
	kotlinOptions {
		jvmTarget = "17"
	}
}

dependencies {
	implementation(project(":shared"))
	implementation(platform("androidx.compose:compose-bom:2024.02.01"))
	implementation("androidx.compose.ui:ui")
	implementation("androidx.compose.ui:ui-tooling")
	implementation("androidx.compose.ui:ui-tooling-preview")
	implementation("androidx.compose.foundation:foundation")
	implementation("androidx.compose.material3:material3")
	implementation("androidx.activity:activity-compose:1.8.2")
	implementation("androidx.core:core-ktx:1.12.0")
}
