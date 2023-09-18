@file:Suppress("UnstableApiUsage")

pluginManagement {
	repositories {
		google()
		mavenCentral()
		gradlePluginPortal()
		maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
		maven("https://maven.pkg.jetbrains.space/kotlin/p/wasm/experimental")
	}
}

dependencyResolutionManagement {
	repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
	repositories {
		google()
		mavenCentral()
		maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
		maven("https://maven.pkg.jetbrains.space/kotlin/p/wasm/experimental")
	}
}

rootProject.name = "Reveal"
includeBuild("convention-plugins")
include(
	":reveal-common",
	":reveal-core",
	":reveal-shapes",
	":reveal-compat-android",
	":android-tests",
)
