plugins {
	`maven-publish`
	signing
}

publishing {
	publications {
		withType<MavenPublication> {
			groupId = "com.svenjacobs.reveal"
			version = (System.getenv("RELEASE_TAG_NAME") ?: "SNAPSHOT").replace("v", "")

			pom {
				afterEvaluate {
					val publicationName: String by extra
					this@pom.name.set(publicationName)
				}
				description.set("Lightweight, simple reveal effect for Jetpack Compose")
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
	}
}

signing {
	// Store key and password in environment variables
	// ORG_GRADLE_PROJECT_signingKey and ORG_GRADLE_PROJECT_signingPassword
	val signingKey: String? by project
	val signingPassword: String? by project

	if (!signingKey.isNullOrEmpty() && !signingPassword.isNullOrEmpty()) {
		useInMemoryPgpKeys(signingKey, signingPassword)
		sign(publishing.publications)
	}
}
