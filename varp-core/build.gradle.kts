import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode

plugins {
    id("varp.build")
    id("varp.publish")
    `java-library`
    alias(libs.plugins.indra.git)
    alias(libs.plugins.blossom)
}

dependencies {
    compileOnly(libs.kotlin.stdlib)
    compileOnly(libs.kotlin.reflect)
    compileOnlyApi(libs.kotlinx.coroutines.core)

    // Libraries
    compileOnlyApi(libs.adventure.api)
    compileOnlyApi(libs.adventure.text.logger.slf4j)

    compileOnlyApi(libs.event)

    // Test implementation
    testImplementation(libs.kotlinx.coroutines.core)
    testImplementation(libs.adventure.api)
    testImplementation(libs.adventure.text.logger.slf4j)
    testImplementation(libs.event)
}

sourceSets {
    main {
        blossom {
            kotlinSources {
                property("version", project.version.toString())
                property("git_commit", indraGit.commit()?.name ?: "<none>")
                property("git_branch", indraGit.branchName() ?: "<none>")
            }
        }
    }
}

kotlin {
    explicitApi = ExplicitApiMode.Strict
}
