import com.github.jengelman.gradle.plugins.shadow.transformers.Log4j2PluginsCacheFileTransformer

plugins {
    id("varp.build")
    alias(libs.plugins.shadow)
    application
}

dependencies {
    // Project
    api(projects.varpCore)
    implementation(projects.varpLoader)
    implementation(projects.varpRepositories.varpRepositoryFileTree)
    implementation(projects.varpRepositories.varpRepositorySql)

    // Libraries
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlin.reflect)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.bundles.adventure)
    implementation(libs.bundles.configurate.core)
    implementation(libs.bundles.configurate.formats)
    implementation(libs.adventure.serializer.configurate4)
    implementation(libs.event)
    implementation(libs.gson)

    // Database
    implementation(libs.bundles.exposed) {
        exclude(group = "org.slf4j", module = "slf4j-api")
    }
    implementation(libs.hikaricp) {
        exclude(group = "org.slf4j", module = "slf4j-api")
    }
    implementation(libs.mysql.connector)
    implementation(libs.postgresql)

    // Logging
    implementation(libs.slf4j.api)
    implementation(libs.log4j.core)
    implementation(libs.log4j.slf4j.impl)

    // Terminal
    implementation(libs.adventure.text.serializer.ansi)
    implementation(libs.bundles.jline)
    implementation(libs.terminalconsoleappender) {
        exclude("org.apache.logging.log4j", module = "log4j-core")
    }

    // Commands
    implementation(libs.bundles.cloud)
}

application {
    mainClass.set("net.voxelpi.varp.cli.MainKt")
}

tasks {
    shadowJar {
        transform(Log4j2PluginsCacheFileTransformer::class.java)
    }

    named<JavaExec>("run") {
        standardInput = System.`in`
    }

    jar {
        manifest {
            attributes(
                "Main-Class" to "net.voxelpi.voxcloud.cloud.MainKt",
                "Multi-Release" to true
            )
        }
    }
}
