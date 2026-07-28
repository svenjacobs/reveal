plugins {
    alias(libs.plugins.android.multiplatform.library)
    id("convention.multiplatform")
    id("convention.publication")
}

extra.set("baseName", "reveal-core")

mavenPublishing {
    pom {
        name.set("Reveal (Core)")
    }
}

val androidMinSdk = rootProject.extra.get("androidMinSdk") as Int
val androidTargetSdk = rootProject.extra.get("androidTargetSdk") as Int
val androidCompileSdk = rootProject.extra.get("androidCompileSdk") as Int

kotlin {
    android {
        namespace = "com.svenjacobs.reveal"
        compileSdk { version = release(androidCompileSdk) }
        minSdk { version = release(androidMinSdk) }

        aarMetadata {
            minCompileSdk = androidMinSdk
        }

        withHostTest {}
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":reveal-common"))
            implementation(libs.compose.multiplatform.runtime)
            implementation(libs.compose.multiplatform.foundation)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

dependencies {
    lintChecks(libs.slack.compose.lint.checks)
}
