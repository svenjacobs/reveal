import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
	alias(libs.plugins.jetbrains.kotlin.multiplatform)
	alias(libs.plugins.jetbrains.compose)
	alias(libs.plugins.compose.compiler)
}

kotlin {
	jvm()
	sourceSets {
		val jvmMain by getting {
			dependencies {
				implementation(compose.desktop.currentOs)
				implementation(project(":shared"))
			}
		}
	}
}

compose.desktop {
	application {
		mainClass = "MainKt"

		nativeDistributions {
			targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
			packageName = "KotlinMultiplatformComposeDesktopApplication"
			packageVersion = "1.0.0"
		}
	}
}
