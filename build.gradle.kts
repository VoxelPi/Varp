plugins {
    alias(libs.plugins.kotlin) apply false
    alias(libs.plugins.dokka)
    alias(libs.plugins.fabric.loom) apply false
}

allprojects {
    group = "net.voxelpi.varp"
    version = "0.1.0-SNAPSHOT"

    repositories {
        mavenCentral()
        maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots") }
        maven { url = uri("https://repo.voxelpi.net/repository/maven-public/") }
        mavenLocal()
    }
}

dependencies {
    dokka(projects.varpApi)
}

dokka {
    dokkaPublicationDirectory.set(layout.buildDirectory.dir("docs"))
}
