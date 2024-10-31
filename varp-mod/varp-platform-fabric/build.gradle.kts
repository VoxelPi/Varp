plugins {
    id("varp.build")
    alias(libs.plugins.shadow)
    alias(libs.plugins.fabric.loom)
}

base {
//    val archivesBaseName: String by project
    archivesName.set("varp-fabric")
}

fun DependencyHandlerScope.modImplementationAndInclude(dep: Any) {
    modImplementation(dep)
    include(dep)
}

fun DependencyHandlerScope.implementationAndInclude(dep: Any) {
    implementation(dep)
    include(dep)
}

fun DependencyHandlerScope.apiAndShadow(dep: Any) {
    api(dep)
    shadow(dep)
}

dependencies {
    compileOnly(kotlin("stdlib"))

    // Project
    apiAndShadow(projects.varpMod.varpModClientCommon)
    apiAndShadow(projects.varpMod.varpModServerCommon)
    apiAndShadow(projects.varpRepositories.varpRepositoryFileTree)

    // Fabric
    minecraft(libs.minecraft)
    mappings(variantOf(libs.yarn) { classifier("v2") })
    modImplementation(libs.fabric.loader)
    modImplementation(libs.fabric.api)
    modImplementation(libs.fabric.language.kotlin)
    modImplementationAndInclude(libs.fabric.permission.api)

    // Libraries
    modImplementationAndInclude(libs.adventure.platform.fabric)
    implementationAndInclude(libs.adventure.serializer.configurate4)
    implementationAndInclude(libs.bundles.cloud)
    modImplementationAndInclude(libs.cloud.fabric)
    implementationAndInclude(libs.bundles.configurate.core)
    implementationAndInclude(libs.bundles.configurate.formats)
    implementationAndInclude(libs.bundles.moonshine)
    implementationAndInclude(libs.event)

    implementationAndInclude(libs.event)
}

loom {
    splitEnvironmentSourceSets()

    mods {
        register("varp-fabric") {
            sourceSet(sourceSets.main.get())
            sourceSet(sourceSets.named("client").get())
        }
    }
}

tasks {
    remapJar {
        dependsOn(shadowJar)
        mustRunAfter(shadowJar)

        // Set the input jar for the task. Here use the shadow Jar that include the .class of the transitive dependency
        inputFile = file(shadowJar.get().archiveFile)
    }

    processResources {
        inputs.property("version", project.version)
        filesMatching("fabric.mod.json") {
            expand(mutableMapOf("version" to project.version))
        }
    }

    shadowJar {
        configurations = listOf(project.configurations.shadow.get())
        exclude("META-INF")
    }
}
