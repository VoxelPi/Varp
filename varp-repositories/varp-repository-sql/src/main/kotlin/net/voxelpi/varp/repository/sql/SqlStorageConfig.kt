package net.voxelpi.varp.repository.sql

@JvmRecord
data class SqlStorageConfig(
    val driver: String,
    val host: String,
    val port: Int,
    val database: String,
    val username: String,
    val password: String,
)
