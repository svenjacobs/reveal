@file:Suppress("UnstableApiUsage")

pluginManagement {
	repositories {
		google()
		mavenCentral()
		gradlePluginPortal()
		maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
		maven("https://maven.pkg.jetbrains.space/kotlin/p/wasm/experimental")
	}

	includeBuild("convention-plugins")
	includeBuild("demo-app")
}

dependencyResolutionManagement {
	repositories {
		google()
		mavenCentral()
		maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
		maven("https://maven.pkg.jetbrains.space/kotlin/p/wasm/experimental")
	}
}

rootProject.name = "Reveal"

include(
	":reveal-common",
	":reveal-core",
	":reveal-shapes",
	":reveal-compat-android",
	":android-tests",
)
