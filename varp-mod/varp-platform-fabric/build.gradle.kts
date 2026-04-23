plugins {
    id("varp.build")
    alias(libs.plugins.shadow)
    alias(libs.plugins.fabric.loom)
}

base {
//    val archivesBaseName: String by project
    archivesName.set("varp-fabric")
}

fun DependencyHandlerScope.implementationAndInclude(dep: Any) {
    implementation(dep)
    include(dep)
}

fun DependencyHandlerScope.apiAndShadow(dep: Any) {
    api(dep)
    shadow(dep)
}

repositories {
    maven { url = uri("https://maven.terraformersmc.com/") }
    maven { url = uri("https://maven.wispforest.io/releases/") }
    maven { url = uri("https://jitpack.io") } // Needed for owo-lib
}

dependencies {
    compileOnly(kotlin("stdlib"))

    // Project
    apiAndShadow(projects.varpMod.varpModClientCommon)
    apiAndShadow(projects.varpMod.varpModServerCommon)
    apiAndShadow(projects.varpRepositories.varpRepositoryFileTree)

    // Fabric
    minecraft(libs.minecraft)
    // mappings(variantOf(libs.yarn) { classifier("v2") })
    implementation(libs.fabric.loader)
    implementation(libs.fabric.api)
    implementation(libs.fabric.language.kotlin)
    implementationAndInclude(libs.fabric.permission.api)

    // Libraries
    implementation(libs.adventure.platform.fabric)
    implementationAndInclude(libs.adventure.serializer.configurate4)
    implementationAndInclude(libs.bundles.cloud)
    implementationAndInclude(libs.cloud.fabric)
    implementationAndInclude(libs.bundles.configurate.core)
    implementationAndInclude(libs.bundles.configurate.formats)
    implementationAndInclude(libs.bundles.moonshine)
    implementationAndInclude(libs.event)

    implementationAndInclude(libs.event)
    implementation(libs.owo.lib) {
        exclude("net.fabricmc.fabric-api")
    }

    // Runtime mods
    runtimeOnly(libs.modmenu)
}

loom {
    splitEnvironmentSourceSets()

    mods {
        register("varp-fabric") {
            sourceSet(sourceSets.main.get())
            sourceSet(sourceSets.named("client").get())
        }
    }

    runs {
        named("client") {
            setIdeConfigGenerated(true)
            setRunDir("run/client")
        }
        named("server") {
            setIdeConfigGenerated(true)
            setRunDir("run/server")
        }
    }
}

tasks {
    jar {
        dependsOn(shadowJar)
        mustRunAfter(shadowJar)

        // Set the input jar for the task. Here use the shadow Jar that include the .class of the transitive dependency
        from({
            zipTree(shadowJar.get().archiveFile.get().asFile)
        })

        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }

    processResources {
        inputs.property("version", version)
        filesMatching("fabric.mod.json") {
            expand(mutableMapOf("version" to version))
        }
    }

    shadowJar {
        configurations = listOf(project.configurations.shadow.get())
        exclude("META-INF")
    }
}
