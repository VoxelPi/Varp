plugins {
    id("varp.build")
    id("varp.publish")
    `java-library`
}

dependencies {
    compileOnly(libs.kotlin.stdlib)

    // Project
    api(projects.varpCore)
    api(projects.varpSerializers.varpSerializerConfigurate)

    // Libraries
    compileOnlyApi(libs.bundles.configurate.core)
    compileOnlyApi(libs.bundles.configurate.formats)
    compileOnlyApi(libs.adventure.text.minimessage)

    testImplementation(libs.kotlin.stdlib)
    testImplementation(libs.kotlin.reflect)
    testImplementation(libs.kotlinx.coroutines.core)
    testImplementation(libs.event)
    testImplementation(libs.bundles.adventure)
    testImplementation(libs.bundles.configurate.core)
    testImplementation(libs.bundles.configurate.formats)
    testImplementation(libs.adventure.text.minimessage)
    testImplementation(libs.adventure.serializer.configurate4)
}
