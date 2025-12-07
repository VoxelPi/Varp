package net.voxelpi.varp.loader.model

import net.voxelpi.varp.repository.RepositoryConfig
import net.voxelpi.varp.repository.RepositoryType

@JvmRecord
internal data class RepositoryDefinition(
    val id: String,
    val type: RepositoryType<*, *>,
    val config: RepositoryConfig,
)
