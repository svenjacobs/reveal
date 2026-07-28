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

fun version(alias: String): String = libs.findVersion(alias).get().requiredVersion

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:${version("kotlin")}")
    implementation("org.jetbrains.compose:compose-gradle-plugin:${version("jetbrains-compose")}")
    implementation(
        "org.jetbrains.kotlin.plugin.compose:" +
            "org.jetbrains.kotlin.plugin.compose.gradle.plugin:${version("kotlin")}",
    )
    implementation("com.android.tools.build:gradle:${version("android-gradle-plugin")}")
    implementation(
        "com.vanniktech:gradle-maven-publish-plugin:${version("vanniktech-maven-publish")}",
    )
    implementation("org.jetbrains.dokka:dokka-gradle-plugin:${version("dokka")}")
}
