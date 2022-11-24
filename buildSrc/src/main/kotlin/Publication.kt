import org.gradle.api.publish.maven.MavenPublication

object Publication {
	const val group = "com.svenjacobs.reveal"
	val version = (System.getenv("RELEASE_TAG_NAME") ?: "SNAPSHOT").let { it.replace("v", "") }
}

fun MavenPublication.pomAttributes() {
	pom {
		name.set("Reveal")
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
