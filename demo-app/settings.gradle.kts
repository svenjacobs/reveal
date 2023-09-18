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

rootProject.name = "Reveal Demo"
include(
	":shared",
	":androidApp",
	":desktopApp",
)
