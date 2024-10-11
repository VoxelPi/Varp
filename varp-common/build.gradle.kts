plugins {
    id("varp.build")
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
                property("git_commit", indraGit.commit()?.name ?: "<none>")
                property("git_branch", indraGit.branchName() ?: "<none>")
            }
        }
    }
}
