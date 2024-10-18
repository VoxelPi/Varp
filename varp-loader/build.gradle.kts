import org.gradle.kotlin.dsl.invoke
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
    api(projects.varpSerializers.varpSerializerGson)

    // Libraries
    compileOnlyApi(libs.gson)
}

kotlin {
    explicitApi = ExplicitApiMode.Strict
}
