import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.compiler)
    id("convention.publication")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

mavenPublishing {
    pom {
        name.set("Reveal (Compat Android)")
    }
}

val androidMinSdk: Int by rootProject.extra
val androidCompileSdk: Int by rootProject.extra

android {
    namespace = "com.svenjacobs.reveal.compat.android"
    compileSdk { version = release(androidCompileSdk) }

    defaultConfig {
        minSdk { version = release(androidMinSdk) }

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
