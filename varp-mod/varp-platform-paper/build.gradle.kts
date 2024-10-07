plugins {
    id("varp.build")
    alias(libs.plugins.paperweight.userdev)
    alias(libs.plugins.run.task)
    alias(libs.plugins.shadow)
}

val mcVersion: String by project
val paperVersion: String by project
val paperApiVersion: String by project

repositories {
    maven { url = uri("https://repo.papermc.io/repository/maven-public/") }
}

dependencies {
    compileOnly(kotlin("stdlib"))

    // Project
    api(projects.varpMod.varpModServerCommon)

    // Paper
    paperweight.paperDevBundle(paperVersion)
}

paperweight.reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.MOJANG_PRODUCTION

tasks {
    assemble {
        dependsOn(shadowJar)
    }

    shadowJar {
        archiveBaseName.set("varp-paper")
        archiveVersion.set("${project.version}")
        archiveClassifier.set("")
    }

    runServer {
        minecraftVersion(mcVersion)
    }

    processResources {
        filesMatching("paper-plugin.yml") {
            expand(
                "version" to project.version,
                "api_version" to paperApiVersion
            )
        }
    }
}
