import com.github.jengelman.gradle.plugins.shadow.transformers.Log4j2PluginsCacheFileTransformer

plugins {
    id("varp.build")
    alias(libs.plugins.shadow)
    application
}

dependencies {
    // Project
    api(projects.varpCore)

    // Libraries
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlin.reflect)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.bundles.adventure)
    implementation(libs.event)

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
