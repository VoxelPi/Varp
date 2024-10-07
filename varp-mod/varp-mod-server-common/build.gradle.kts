plugins {
    id("varp.build")
    `java-library`
}

dependencies {
    compileOnly(kotlin("stdlib"))

    // Project
    api(projects.varpMod.varpModServerApi)
    api(projects.varpMod.varpModCommon)

    // Libraries
    compileOnlyApi(libs.bundles.cloud)
}
