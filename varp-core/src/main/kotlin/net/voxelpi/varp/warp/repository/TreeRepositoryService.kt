package net.voxelpi.varp.warp.repository

public class TreeRepositoryService internal constructor() {

    private val repositories: MutableMap<String, TreeRepository> = mutableMapOf()

    public fun repositories(): Map<String, TreeRepository> {
        return repositories
    }

    public fun repository(name: String): TreeRepository? {
        return repositories[name]
    }

    public fun registerRepository(name: String, repository: TreeRepository) {
        this.repositories[name] = repository
    }

    public fun unregisterRepository(name: String): TreeRepository? {
        return this.repositories.remove(name)
    }
}