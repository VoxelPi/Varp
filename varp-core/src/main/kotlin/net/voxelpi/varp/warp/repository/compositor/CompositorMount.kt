package net.voxelpi.varp.warp.repository.compositor

import net.voxelpi.varp.warp.path.NodeParentPath
import net.voxelpi.varp.warp.repository.Repository

/**
 * Specifies a mount of a tree compositor.
 * @property path the path where the repository should be mounted.
 * @property repository the repository that should be mounted.
 */
public data class CompositorMount(
    val path: NodeParentPath,
    val repository: Repository,
)
