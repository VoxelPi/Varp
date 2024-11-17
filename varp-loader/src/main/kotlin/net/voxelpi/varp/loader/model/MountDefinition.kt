package net.voxelpi.varp.loader.model

import net.voxelpi.varp.warp.path.NodeParentPath

@JvmRecord
internal data class MountDefinition(
    val location: NodeParentPath,
    val repository: String,
)
