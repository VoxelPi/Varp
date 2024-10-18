package net.voxelpi.varp.warp.repository

import java.nio.file.Path

public class RepositoryService internal constructor() {

    private val repositories: MutableMap<String, Repository> = mutableMapOf()

    public fun repositories(): Map<String, Repository> {
        return repositories
    }

    public fun repository(name: String): Repository? {
        return repositories[name]
    }

    public fun registerRepository(name: String, repository: Repository) {
        this.repositories[name] = repository
    }

    public fun unregisterRepository(name: String): Repository? {
        return this.repositories.remove(name)
    }

    public fun clearRepositories() {
        repositories.clear()
    }

    public fun loadRepositories(path: Path): Collection<Repository> {
        return emptyList()
    }
}
