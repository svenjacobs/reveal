plugins {
    alias(libs.plugins.android.multiplatform.library)
    id("convention.multiplatform")
    id("convention.publication")
}

extra.set("baseName", "reveal-shapes")

mavenPublishing {
    pom {
        name.set("Reveal (Shapes)")
    }
}

val androidMinSdk = rootProject.extra.get("androidMinSdk") as Int
val androidCompileSdk = rootProject.extra.get("androidCompileSdk") as Int

kotlin {
    android {
        namespace = "com.svenjacobs.reveal.shapes"
        compileSdk { version = release(androidCompileSdk) }
        minSdk { version = release(androidMinSdk) }

        aarMetadata {
            minCompileSdk = androidMinSdk
        }

        withHostTest {}
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":reveal-common"))
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
