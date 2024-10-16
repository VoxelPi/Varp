import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode

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
    compileOnlyApi(libs.bundles.configurate.core)
    compileOnlyApi(libs.adventure.serializer.configurate4)
}

kotlin {
    explicitApi = ExplicitApiMode.Strict
}
