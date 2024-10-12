package net.voxelpi.varp

import net.voxelpi.varp.warp.Tree
import net.voxelpi.varp.warp.provider.TreeProvider

/**
 * Provides varp api.
 */
public object Varp {

    public val version: String
        get() = VarpBuildParameters.VERSION

    public val exactVersion: String
        get() = "${VarpBuildParameters.VERSION}-${VarpBuildParameters.GIT_COMMIT}"

    public fun createTree(provider: TreeProvider): Tree {
        return Tree(provider)
    }
}
