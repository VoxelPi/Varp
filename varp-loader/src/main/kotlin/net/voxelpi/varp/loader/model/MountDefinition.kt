package net.voxelpi.varp.loader.model

import net.voxelpi.varp.warp.path.NodeParentPath

@JvmRecord
internal data class MountDefinition(
    val path: NodeParentPath,
    val repository: String,
    val sourcePath: NodeParentPath,
)
