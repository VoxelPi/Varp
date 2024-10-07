plugins {
    id("varp.build")
    `java-library`
}

dependencies {
    compileOnly(kotlin("stdlib"))

    // Project
    api(projects.varpMod.varpModClientApi)
    api(projects.varpMod.varpModCommon)
}
