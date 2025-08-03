package net.voxelpi.varp.repository.sql

import net.voxelpi.varp.warp.repository.RepositoryConfig

data class SqlRepositoryConfig(
    val host: String,
    val port: Int,
    val database: String,
    val username: String,
    val password: String,
) : RepositoryConfig
