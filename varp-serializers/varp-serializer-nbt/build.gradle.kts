import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode

plugins {
    id("varp.build")
    id("varp.publish")
    `java-library`
}

dependencies {
    compileOnly(libs.kotlin.stdlib)

    // Project
    api(projects.varpCore)

    // Libraries
    compileOnlyApi(libs.adventure.nbt)

    testImplementation(libs.kotlin.stdlib)
    testImplementation(libs.kotlin.reflect)
    testImplementation(libs.kotlinx.coroutines.core)
    testImplementation(libs.event)
    testImplementation(libs.bundles.adventure)
    testImplementation(libs.adventure.nbt)
    testImplementation(libs.bundles.configurate.core)
    testImplementation(libs.bundles.configurate.formats)
    testImplementation(libs.adventure.text.minimessage)
}

kotlin {
    explicitApi = ExplicitApiMode.Strict
}
