import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

plugins {
	alias(libs.plugins.android.application) apply false
	alias(libs.plugins.android.library) apply false
	alias(libs.plugins.jetbrains.kotlin.android) apply false
	alias(libs.plugins.nexus.publish)
	alias(libs.plugins.ben.manes.versions)
	alias(libs.plugins.kotlinter)
}

group = Publication.group
version = Publication.version

subprojects {
	apply(plugin = "org.jmailen.kotlinter")
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
