plugins {
	alias(libs.plugins.android.application)
	alias(libs.plugins.jetbrains.kotlin.android)
	alias(libs.plugins.compose.compiler)
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
		kotlinCompilerExtensionVersion = "1.5.14"
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
	implementation(platform(libs.androidx.compose.bom))
	implementation(libs.androidx.compose.ui)
	implementation(libs.androidx.compose.ui.tooling)
	implementation(libs.androidx.compose.ui.tooling.preview)
	implementation(libs.androidx.compose.foundation)
	implementation(libs.androidx.compose.material3)
	implementation(libs.androidx.activity.compose)
	implementation(libs.androidx.core.ktx)
}
