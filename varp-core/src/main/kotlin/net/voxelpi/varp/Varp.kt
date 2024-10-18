package net.voxelpi.varp

import net.voxelpi.varp.warp.Tree
import net.voxelpi.varp.warp.repository.Repository
import net.voxelpi.varp.warp.repository.RepositoryService

/**
 * Provides varp api.
 */
public object Varp {

    public val version: String
        get() = VarpBuildParameters.VERSION

    public val exactVersion: String
        get() = "${VarpBuildParameters.VERSION}-${VarpBuildParameters.GIT_COMMIT}"

    public val repositoryService: RepositoryService = RepositoryService()

    public fun createTree(repository: Repository): Tree {
        return Tree(repository)
    }
}
