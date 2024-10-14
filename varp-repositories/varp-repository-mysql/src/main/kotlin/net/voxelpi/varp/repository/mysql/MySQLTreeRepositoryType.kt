package net.voxelpi.varp.repository.mysql

import net.voxelpi.varp.warp.repository.TreeRepositoryType

object MySQLTreeRepositoryType : TreeRepositoryType<MySQLTreeRepository, MySQLTreeRepositoryConfig> {

    override val id: String = "mysql"

    override fun createRepository(id: String, config: MySQLTreeRepositoryConfig): MySQLTreeRepository {
        return MySQLTreeRepository(id, config.hostname, config.port, config.database, config.username, config.password)
    }
}
