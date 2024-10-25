plugins {
    id("varp.build")
    id("varp.publish")
    `java-library`
}

dependencies {
    compileOnly(libs.kotlin.stdlib)

    // Project
    compileOnlyApi(projects.varpCore)

    // Libraries
    compileOnlyApi(libs.bundles.adventure)
    compileOnly(libs.bundles.exposed)
    compileOnly(libs.mysql.connector)
}
