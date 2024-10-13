package net.voxelpi.varp

import net.voxelpi.varp.warp.Tree
import net.voxelpi.varp.warp.repository.TreeRepository
import net.voxelpi.varp.warp.repository.TreeRepositoryService

/**
 * Provides varp api.
 */
public object Varp {

    public val version: String
        get() = VarpBuildParameters.VERSION

    public val exactVersion: String
        get() = "${VarpBuildParameters.VERSION}-${VarpBuildParameters.GIT_COMMIT}"

    public val treeRepositoryService: TreeRepositoryService = TreeRepositoryService()

    public fun createTree(repository: TreeRepository): Tree {
        return Tree(repository)
    }
}
