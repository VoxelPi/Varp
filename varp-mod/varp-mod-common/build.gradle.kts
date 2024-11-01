plugins {
    id("varp.build")
    `java-library`
}

dependencies {
    compileOnly(libs.kotlin.stdlib)
    compileOnly(libs.kotlin.reflect)

    // Project
    api(projects.varpCore)
    api(projects.varpMod.varpModApi)
    api(projects.varpSerializers.varpSerializerGson)

    // Libraries
    compileOnlyApi(libs.adventure.text.serializer.gson)
}
