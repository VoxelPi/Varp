package net.voxelpi.varp.repository.sql

import com.zaxxer.hikari.HikariDataSource
import net.voxelpi.event.EventScope
import net.voxelpi.varp.repository.StorageHandle

@JvmRecord
data class SqlStorageHandle(
    override val eventScope: EventScope,
    val dataSource: HikariDataSource,
) : StorageHandle {

    /**
     * Check if the repository is currently connected to the database.
     */
    fun isConnected(): Boolean {
        this.dataSource.connection.use { connection ->
            return connection.isValid(5)
        }
    }
}
