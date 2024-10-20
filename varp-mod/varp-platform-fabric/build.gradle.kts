plugins {
    id("varp.build")
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

fun DependencyHandlerScope.apiAndInclude(dep: Any) {
    api(dep)
    include(dep)
}

fun DependencyHandlerScope.implementationAndInclude(dep: Any) {
    implementation(dep)
    include(dep)
}

dependencies {
    compileOnly(kotlin("stdlib"))

    // Project
    apiAndInclude(projects.varpMod.varpModClientCommon)
    apiAndInclude(projects.varpMod.varpModServerCommon)
    apiAndInclude(projects.varpRepositories.varpRepositoryFileTree)

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
    processResources {
        inputs.property("version", project.version)
        filesMatching("fabric.mod.json") {
            expand(mutableMapOf("version" to project.version))
        }
    }
}
