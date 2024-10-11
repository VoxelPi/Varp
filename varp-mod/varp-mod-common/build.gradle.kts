plugins {
    id("varp.build")
    `java-library`
}

dependencies {
    compileOnly(kotlin("stdlib"))

    // Project
    api(projects.varpCore)
}
