import org.gradle.accessors.dm.LibrariesForLibs
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

val libs = the<LibrariesForLibs>()

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.dokka")
    id("org.jlleitschuh.gradle.ktlint")
}

repositories {
    mavenCentral()
}

dependencies {
    // Tests
    testImplementation(kotlin("stdlib"))
    testImplementation(libs.kotlin.reflect)
    testImplementation(kotlin("test"))
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)

    testImplementation(libs.slf4j.api)
    testImplementation(libs.log4j.slf4j.impl)
}

plugins.withId("org.jetbrains.kotlin.jvm") {
    extensions.configure<KotlinJvmProjectExtension>("kotlin") {
        jvmToolchain(25)
        compilerOptions {}
    }
}

dokka {
    basePublicationsDirectory.set(layout.buildDirectory.dir("docs"))
}

tasks {
    test {
        useJUnitPlatform()
    }

    ktlint {
        version.set("1.0.0")
        verbose.set(true)
        outputToConsole.set(true)
        coloredOutput.set(true)
        reporters {
            reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.CHECKSTYLE)
        }
    }
}
