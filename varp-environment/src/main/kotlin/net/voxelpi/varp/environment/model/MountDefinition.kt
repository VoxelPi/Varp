package net.voxelpi.varp.environment.model

import net.voxelpi.varp.tree.path.NodeParentPath

@JvmRecord
public data class MountDefinition(
    val repository: String,
    val path: NodeParentPath,
)
