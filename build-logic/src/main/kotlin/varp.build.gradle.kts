import org.gradle.accessors.dm.LibrariesForLibs
import org.jetbrains.kotlin.gradle.dsl.JvmDefaultMode

val libs = the<LibrariesForLibs>()
val javaVersion = JavaVersion.VERSION_21

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

kotlin {
    jvmToolchain(21)
    compilerOptions {
        jvmDefault = JvmDefaultMode.NO_COMPATIBILITY
    }
}

java {
    // Configure the java toolchain. This allows gradle to auto-provision JDK 21 on systems that only have JDK 8 installed for example.
    toolchain.languageVersion.set(JavaLanguageVersion.of(javaVersion.toString()))
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion
}

dokka {
    basePublicationsDirectory.set(layout.buildDirectory.dir("docs"))
}

tasks {
    withType<JavaCompile> {
        options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything

        sourceCompatibility = javaVersion.toString()
        targetCompatibility = javaVersion.toString()

        // Set the release flag. This configures what version bytecode the compiler will emit, as well as what JDK APIs are usable.
        // See https://openjdk.java.net/jeps/247 for more information.
        options.release.set(javaVersion.toString().toInt())
    }

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
