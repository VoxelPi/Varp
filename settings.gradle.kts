rootProject.name = "varp"

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.fabricmc.net/") { name = "Fabric" }
    }
    includeBuild("build-logic")
}

include("varp-api")
include("varp-common")
include("varp-mod:varp-mod-common")
include("varp-mod:varp-mod-client-api")
include("varp-mod:varp-mod-client-common")
include("varp-mod:varp-mod-server-api")
include("varp-mod:varp-mod-server-common")
include("varp-mod:varp-platform-fabric")
include("varp-mod:varp-platform-paper")

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
include("varp-mod:varp-mod-client-api")
findProject(":varp-mod:varp-mod-client-api")?.name = "varp-mod-client-api"
