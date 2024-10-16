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
    compileOnlyApi(libs.gson)
}

kotlin {
    explicitApi = ExplicitApiMode.Strict
}
