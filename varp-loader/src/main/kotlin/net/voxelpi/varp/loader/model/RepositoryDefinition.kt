package net.voxelpi.varp.loader.model

import net.voxelpi.varp.warp.repository.RepositoryConfig
import net.voxelpi.varp.warp.repository.RepositoryType

internal data class RepositoryDefinition(
    val id: String,
    val type: RepositoryType<*, *>,
    val config: RepositoryConfig,
)
