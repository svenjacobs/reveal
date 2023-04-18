@file:Suppress("UnstableApiUsage")

pluginManagement {
	repositories {
		google()
		mavenCentral()
		gradlePluginPortal()
	}
}

dependencyResolutionManagement {
	repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
	repositories {
		google()
		mavenCentral()
	}
}

rootProject.name = "Reveal"
include(
	":reveal-core",
	":reveal-shapes",
	":demo-android",
)
