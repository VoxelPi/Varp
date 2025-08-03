package net.voxelpi.varp.repository.sql

import com.zaxxer.hikari.HikariConfig
import net.voxelpi.varp.warp.repository.RepositoryType

abstract class SqlRepositoryType(
    id: String,
) : RepositoryType<SqlRepository, SqlRepositoryConfig>(id, SqlRepository::class, SqlRepositoryConfig::class) {

    object PostgreSql : SqlRepositoryType("postgresql") {

        override fun create(id: String, config: SqlRepositoryConfig): Result<SqlRepository> {
            val hikariConfig = HikariConfig().apply {
                jdbcUrl = "jdbc:postgresql://${config.host}:${config.port}/${config.database}"
                username = config.username
                password = config.password

                poolName = "varp"
                addDataSourceProperty("tcpKeepAlive", "true")
            }

            return Result.success(SqlRepository(id, config, this, hikariConfig))
        }
    }

    object MySql : SqlRepositoryType("mysql") {

        override fun create(id: String, config: SqlRepositoryConfig): Result<SqlRepository> {
            val hikariConfig = HikariConfig().apply {
                driverClassName = "com.mysql.cj.jdbc.Driver"
                jdbcUrl = "jdbc:mysql://${config.host}:${config.port}/${config.database}?useSSL=false&serverTimezone=UTC"
                username = config.username
                password = config.password

                poolName = "varp"
            }

            return Result.success(SqlRepository(id, config, this, hikariConfig))
        }
    }
}
