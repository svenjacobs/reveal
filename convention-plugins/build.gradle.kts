val catalogs = extensions.getByType<VersionCatalogsExtension>()
val libs: VersionCatalog = catalogs.named("libs")

plugins {
	`kotlin-dsl`
}

repositories {
	google()
	mavenCentral()
	gradlePluginPortal()
}

dependencies {
	implementation(
		group = "org.jetbrains.kotlin",
		name = "kotlin-gradle-plugin",
		version = libs.findVersion("kotlin").get().requiredVersion,
	)
	implementation(
		group = "org.jetbrains.compose",
		name = "compose-gradle-plugin",
		version = libs.findVersion("jetbrains-compose").get().requiredVersion,
	)
}
