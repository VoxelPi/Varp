import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode

plugins {
    id("varp.build")
    id("varp.publish")
    `java-library`
}

dependencies {
    // Kotlin
    compileOnly(libs.kotlin.stdlib)
    compileOnly(libs.kotlin.reflect)
    compileOnlyApi(libs.kotlinx.coroutines.core)

    // Project
    compileOnlyApi(projects.varpCore)

    // Libraries
    compileOnlyApi(libs.adventure.api)
    compileOnlyApi(libs.adventure.text.logger.slf4j)

    compileOnlyApi(libs.event)

    // Commands
    implementation(libs.bundles.cloud)

    // Test implementation
    testImplementation(libs.kotlinx.coroutines.core)
    testImplementation(libs.adventure.api)
    testImplementation(libs.adventure.text.logger.slf4j)
    testImplementation(libs.event)
}

kotlin {
    explicitApi = ExplicitApiMode.Strict
}
