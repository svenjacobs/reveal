plugins {
    // Dokka HTML is picked up automatically as the javadoc jar by the publish plugin.
    // Dokka's Javadoc format does not support Kotlin Multiplatform.
    id("org.jetbrains.dokka")
    id("com.vanniktech.maven.publish")
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()

    // artifactId is left at its default (the project name)
    coordinates(
        groupId = "com.svenjacobs.reveal",
        version = (System.getenv("RELEASE_TAG_NAME") ?: "0.0.1-SNAPSHOT").replace("v", ""),
    )

    pom {
        // name is set per module
        description.set("Lightweight, simple reveal effect for Compose Multiplatform")
        url.set("https://github.com/svenjacobs/reveal")

        developers {
            developer {
                id.set("svenjacobs")
                name.set("Sven Jacobs")
                email.set("github@svenjacobs.com")
                url.set("https://svenjacobs.com/")
                timezone.set("GMT+1")
            }
        }

        licenses {
            license {
                name.set("MIT License")
                url.set("https://opensource.org/licenses/MIT")
            }
        }

        scm {
            connection.set("scm:git:git://github.com/svenjacobs/reveal.git")
            developerConnection.set("scm:git:git://github.com/svenjacobs/reveal.git")
            url.set("https://github.com/svenjacobs/reveal")
        }
    }
}

// Store key and password in environment variables
// ORG_GRADLE_PROJECT_signingInMemoryKey and ORG_GRADLE_PROJECT_signingInMemoryKeyPassword
//
// Set locally for testing:
//   export ORG_GRADLE_PROJECT_signingInMemoryKey="$(gpg --export-secret-keys --armor mail@address)"
//   export ORG_GRADLE_PROJECT_signingInMemoryKeyPassword="password"
