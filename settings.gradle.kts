rootProject.name = "varp"

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.fabricmc.net/") { name = "Fabric" }
    }
    includeBuild("build-logic")
}

include("varp-core")
include("varp-loader")
include("varp-repositories:varp-repository-file-tree")
include("varp-repositories:varp-repository-sql")
include("varp-serializers:varp-serializer-configurate")
include("varp-serializers:varp-serializer-gson")
include("varp-extras:varp-extras-cloud")
include("varp-cli")
include("varp-mod:varp-mod-api")
include("varp-mod:varp-mod-common")
include("varp-mod:varp-mod-client-api")
include("varp-mod:varp-mod-client-common")
include("varp-mod:varp-mod-server-api")
include("varp-mod:varp-mod-server-common")
include("varp-mod:varp-platform-fabric")
include("varp-mod:varp-platform-paper")

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
