plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.dokka")
    id("org.jetbrains.dokka-javadoc")
    `maven-publish`
    signing
}

val javadocJar by tasks.register<Jar>("dokkaJavadocJar") {
    dependsOn(tasks.dokkaGenerate)
    from(tasks.dokkaGeneratePublicationJavadoc.flatMap { it.outputDirectory })
    archiveClassifier.set("javadoc")
}

val sourcesJar by tasks.creating(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.getByName("main").allSource)
}

publishing {
    repositories {
        maven {
            name = "VoxelPiRepo"
            val releasesRepoUrl = "https://repo.voxelpi.net/repository/maven-releases/"
            val snapshotsRepoUrl = "https://repo.voxelpi.net/repository/maven-snapshots/"
            url = uri(if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl)
            credentials {
                username = findProperty("vpr.user") as String? ?: System.getenv("VOXELPI_REPO_USER")
                password = findProperty("vpr.key") as String? ?: System.getenv("VOXELPI_REPO_KEY")
            }
        }
    }

    publications {
        create<MavenPublication>("maven") {
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()

            from(components["kotlin"])
            artifact(sourcesJar)
            artifact(javadocJar)

            pom {
                name = project.name
                description = project.description
                url = "https://github.com/voxelpi/varp"

                licenses {
                    license {
                        name = "The MIT License"
                        url = "https://opensource.org/licenses/MIT"
                    }
                }

                developers {
                    developer {
                        id = "voxelpi"
                        name = "Peter Smek"
                        url = "https://voxelpi.net"
                    }
                }

                scm {
                    connection = "scm:git:https://github.com/voxelpi/varp.git"
                    developerConnection = "scm:git:ssh://git@github.com/voxelpi/varp.git"
                    url = "https://github.com/voxelpi/varp"
                }

                issueManagement {
                    system = "GitHub"
                    url = "https://github.com/voxelpi/varp/issues"
                }

                ciManagement {
                    system = "GitHub Actions"
                    url = "https://github.com/voxelpi/varp/actions"
                }
            }
        }
    }
}

signing {
    val signingSecretKey = System.getenv("SIGNING_KEY")
    val signingPassword = System.getenv("SIGNING_PASSWORD")
    if (signingSecretKey != null && signingPassword != null) {
        useInMemoryPgpKeys(signingSecretKey, signingPassword)
    } else {
        useGpgCmd()
    }
    sign(publishing.publications)
}
