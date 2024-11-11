package net.voxelpi.varp.repository.mysql

import net.voxelpi.varp.warp.repository.RepositoryType

object MySQLRepositoryType : RepositoryType<MySQLRepository, MySQLRepositoryConfig>("mysql", MySQLRepository::class, MySQLRepositoryConfig::class) {

    override fun create(id: String, config: MySQLRepositoryConfig): Result<MySQLRepository> {
        return Result.success(MySQLRepository(id, config))
    }
}
