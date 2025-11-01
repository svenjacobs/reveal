pluginManagement {
	repositories {
		google()
		gradlePluginPortal()
		mavenCentral()
	}
}

plugins {
	id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
	repositories {
		google()
		mavenCentral()
		mavenLocal()
	}
}

rootProject.name = "RevealDemo"
include(
	":shared",
	":androidApp",
	":desktopApp",
)

includeBuild("../") {
	name = "reveal-root"
	dependencySubstitution {
		substitute(module("reveal:core")).using(project(":reveal-core"))
		substitute(module("reveal:shapes")).using(project(":reveal-shapes"))
	}
}
