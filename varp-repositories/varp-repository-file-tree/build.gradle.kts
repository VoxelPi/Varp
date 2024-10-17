plugins {
    id("varp.build")
    id("varp.publish")
    `java-library`
}

dependencies {
    compileOnly(libs.kotlin.stdlib)

    // Project
    compileOnlyApi(projects.varpCore)
    compileOnlyApi(projects.varpSerializers.varpSerializerConfigurate)

    // Libraries
    compileOnlyApi(libs.bundles.configurate.core)
    compileOnlyApi(libs.bundles.configurate.formats)
}
