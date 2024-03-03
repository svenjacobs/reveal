pluginManagement {
	repositories {
		google()
		gradlePluginPortal()
		mavenCentral()
	}
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
