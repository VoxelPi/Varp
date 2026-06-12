package net.voxelpi.varp.environment

import net.voxelpi.varp.tree.path.NodeParentPath
import net.voxelpi.varp.tree.state.FolderState

@JvmRecord
public data class MountDefinition(
    val repository: String,
    val sourcePath: NodeParentPath,
    val state: FolderState,
)
