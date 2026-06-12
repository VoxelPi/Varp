package net.voxelpi.varp.compositor

import net.voxelpi.varp.repository.Repository
import net.voxelpi.varp.tree.path.NodeParentPath
import net.voxelpi.varp.tree.state.FolderState

/**
 * Specifies a mount of a tree compositor.
 * @property targetPath The path where the repository should be mounted.
 * @property repository The repository that should be mounted.
 * @property sourcePath The path of the container in the repository that is mounted to the tree.
 * @property state The state of the mount root folder.
 */
@JvmRecord
public data class CompositorMount(
    val targetPath: NodeParentPath,
    val repository: Repository<*, *>,
    val sourcePath: NodeParentPath,
    val state: FolderState = FolderState.defaultMountState(),
)
