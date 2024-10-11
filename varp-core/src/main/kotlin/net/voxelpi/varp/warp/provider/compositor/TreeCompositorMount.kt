package net.voxelpi.varp.warp.provider.compositor

import net.voxelpi.varp.warp.path.NodeParentPath
import net.voxelpi.varp.warp.provider.TreeProvider

/**
 * Specifies a mount in a tree compositor.
 * @property location the location where the storage should be mounted.
 * @property provider the provider that should be mounted.
 */
public data class TreeCompositorMount(
    val location: NodeParentPath,
    val provider: TreeProvider,
)
