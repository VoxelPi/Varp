package net.voxelpi.varp.api.warp.provider.compositor

import net.voxelpi.varp.api.warp.path.NodeParentPath
import net.voxelpi.varp.api.warp.provider.TreeProvider

/**
 * Specifies a mount in a tree compositor.
 * @property location the location where the storage should be mounted.
 * @property provider the provider that should be mounted.
 */
data class TreeCompositorMount(
    val location: NodeParentPath,
    val provider: TreeProvider,
)
