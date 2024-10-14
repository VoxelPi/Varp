package net.voxelpi.varp.repository.mysql

import net.voxelpi.varp.warp.repository.TreeRepositoryConfig

data class MySQLTreeRepositoryConfig(
    val hostname: String,
    val port: Int,
    val database: String,
    val username: String,
    val password: String,
) : TreeRepositoryConfig
