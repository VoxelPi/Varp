import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode

plugins {
    id("varp.build")
    id("varp.publish")
    `java-library`
}

dependencies {
    compileOnly(kotlin("stdlib"))

    // Project
    api(projects.varpCore)
    api(projects.varpLoader)
}

kotlin {
    explicitApi = ExplicitApiMode.Strict
}
