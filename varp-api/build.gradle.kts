plugins {
    id("varp.build")
    id("varp.publish")
    `java-library`
}

dependencies {
    compileOnly(kotlin("stdlib"))

    // Libraries
    compileOnlyApi(libs.adventure.api)
    compileOnlyApi(libs.adventure.text.logger.slf4j)
}
