package net.voxelpi.varp.warp.repository.compositor

import net.voxelpi.varp.warp.path.NodeParentPath
import net.voxelpi.varp.warp.repository.Repository

/**
 * Specifies a mount of a tree compositor.
 * @property path The path where the repository should be mounted.
 * @property repository The repository that should be mounted.
 * @property sourcePath The path of the container in the repository that is mounted to the tree.
 */
public data class CompositorMount(
    val path: NodeParentPath,
    val repository: Repository,
    val sourcePath: NodeParentPath,
)
