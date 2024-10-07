plugins {
    id("varp.build")
    id("varp.publish")
    alias(libs.plugins.indra.git)
    alias(libs.plugins.blossom)
}

dependencies {
    compileOnly(kotlin("stdlib"))

    // Project
    api(projects.varpApi)
}

sourceSets {
    main {
        blossom {
            kotlinSources {
                property("version", project.version.toString())
                property("git_commit", indraGit.commit()?.name)
            }
        }
    }
}
