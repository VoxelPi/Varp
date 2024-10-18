package net.voxelpi.varp.warp.repository.compositor

import net.voxelpi.varp.warp.path.NodeParentPath
import net.voxelpi.varp.warp.repository.Repository

/**
 * Specifies a mount in a tree compositor.
 * @property location the location where the storage should be mounted.
 * @property repository the repository that should be mounted.
 */
public data class TreeCompositorMount(
    val location: NodeParentPath,
    val repository: Repository,
)
