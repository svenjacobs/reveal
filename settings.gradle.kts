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
}

plugins {
	id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
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
