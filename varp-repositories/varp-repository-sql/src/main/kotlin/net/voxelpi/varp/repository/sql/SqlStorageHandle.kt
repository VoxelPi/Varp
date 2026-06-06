package net.voxelpi.varp.repository.sql

import com.zaxxer.hikari.HikariDataSource

@JvmRecord
data class SqlStorageHandle(
    val dataSource: HikariDataSource,
) {

    /**
     * Check if the repository is currently connected to the database.
     */
    fun isConnected(): Boolean {
        this.dataSource.connection.use { connection ->
            return connection.isValid(5)
        }
    }
}
