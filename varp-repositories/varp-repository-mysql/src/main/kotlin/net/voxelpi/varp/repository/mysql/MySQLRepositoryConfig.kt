package net.voxelpi.varp.repository.mysql

import net.voxelpi.varp.warp.repository.RepositoryConfig

data class MySQLRepositoryConfig(
    val hostname: String,
    val port: Int,
    val database: String,
    val username: String,
    val password: String,
) : RepositoryConfig
