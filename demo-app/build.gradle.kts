import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

plugins {
	id("com.android.application").version("8.1.2").apply(false)
	id("com.android.library").version("8.1.2").apply(false)
	kotlin("android").version("1.9.10").apply(false)
	kotlin("multiplatform").version("1.9.10").apply(false)
	id("org.jetbrains.compose").version("1.5.2").apply(false)
	id("com.github.ben-manes.versions").version("0.48.0")
}

tasks.register("clean", Delete::class) {
	delete(rootProject.layout.buildDirectory)
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
