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
    implementation(
        group = "org.jetbrains.kotlin.plugin.compose",
        name = "org.jetbrains.kotlin.plugin.compose.gradle.plugin",
        version = libs.findVersion("kotlin").get().requiredVersion,
    )
    implementation(
        group = "com.android.tools.build",
        name = "gradle",
        version = libs.findVersion("android-gradle-plugin").get().requiredVersion,
    )
    implementation(
        group = "com.vanniktech",
        name = "gradle-maven-publish-plugin",
        version = libs.findVersion("vanniktech-maven-publish").get().requiredVersion,
    )
    implementation(
        group = "org.jetbrains.dokka",
        name = "dokka-gradle-plugin",
        version = libs.findVersion("dokka").get().requiredVersion,
    )
}
