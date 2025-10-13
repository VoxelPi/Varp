plugins {
    id("varp.build")
    `java-library`
}

dependencies {
    compileOnly(kotlin("stdlib"))

    // Project
    api(projects.varpMod.varpModServerApi)
    api(projects.varpMod.varpModCommon)
    api(projects.varpExtras.varpExtrasCloud)

    // Libraries
    compileOnlyApi(libs.bundles.cloud)
    compileOnlyApi(libs.bundles.configurate.core)
    compileOnlyApi(libs.bundles.configurate.formats)
    compileOnlyApi(libs.bundles.moonshine)
}
