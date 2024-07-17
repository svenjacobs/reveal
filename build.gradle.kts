import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

buildscript {
	dependencies {
	}
}

plugins {
	alias(libs.plugins.jetbrains.kotlin.multiplatform) apply false
	alias(libs.plugins.jetbrains.compose) apply false
	alias(libs.plugins.android.application) apply false
	alias(libs.plugins.android.library) apply false
	alias(libs.plugins.jetbrains.kotlin.android) apply false
	alias(libs.plugins.nexus.publish)
	alias(libs.plugins.ben.manes.versions)
	alias(libs.plugins.kotlinter)
	alias(libs.plugins.binary.compat.validator)
}

group = "com.svenjacobs.reveal"
version = (System.getenv("RELEASE_TAG_NAME") ?: "SNAPSHOT").replace("v", "")

subprojects {
	apply(plugin = "org.jmailen.kotlinter")
}

apiValidation {
	ignoredProjects += listOf(
		"android-tests",
	)
}

nexusPublishing {
	repositories {
		sonatype()
	}
}

tasks.withType<DependencyUpdatesTask> {

	fun isNonStable(version: String) =
		listOf("alpha", "beta", "rc", "eap", "-m", ".m", "-a", "dev").any {
			version.lowercase().contains(it)
		}

	rejectVersionIf {
		isNonStable(candidate.version) && !isNonStable(currentVersion)
	}
}

val androidMinSdk by extra { 21 }
val androidTargetSdk by extra { 34 }
val androidCompileSdk by extra { 34 }
