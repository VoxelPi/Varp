package net.voxelpi.varp.repository.sql

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import net.kyori.adventure.key.Key
import net.voxelpi.event.eventScope
import net.voxelpi.varp.ComponentTemplate
import net.voxelpi.varp.MinecraftLocation
import net.voxelpi.varp.repository.Storage
import net.voxelpi.varp.repository.StorageCapability
import net.voxelpi.varp.repository.sql.function.ReplaceFunction
import net.voxelpi.varp.repository.sql.table.FolderTable
import net.voxelpi.varp.repository.sql.table.WarpTable
import net.voxelpi.varp.tree.path.FolderPath
import net.voxelpi.varp.tree.path.NodeParentPath
import net.voxelpi.varp.tree.path.RootPath
import net.voxelpi.varp.tree.path.WarpPath
import net.voxelpi.varp.tree.state.FolderState
import net.voxelpi.varp.tree.state.MutableTreeState
import net.voxelpi.varp.tree.state.TreeState
import net.voxelpi.varp.tree.state.WarpState
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.like
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.insertIgnore
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import java.io.IOException
import java.util.EnumSet
import kotlin.reflect.KClass

object SqlStorage : Storage<SqlStorageConfig, SqlStorageHandle> {

    override val capabilities: EnumSet<StorageCapability> = EnumSet.of(
        StorageCapability.RECURSIVE_DELETE,
        StorageCapability.RECURSIVE_MOVE,
    )

    override val configType: KClass<SqlStorageConfig>
        get() = SqlStorageConfig::class

    override suspend fun open(config: SqlStorageConfig): Result<SqlStorageHandle> = runCatching {
        // Create a configuration depending on the driver.
        val hikariConfig = HikariConfig().apply {
            when (config.driver) {
                "mysql" -> {
                    driverClassName = "com.mysql.cj.jdbc.Driver"
                    jdbcUrl = "jdbc:mysql://${config.host}:${config.port}/${config.database}?useSSL=false&serverTimezone=UTC"
                    username = config.username
                    password = config.password
                }
                "postgres" -> {
                    jdbcUrl = "jdbc:postgresql://${config.host}:${config.port}/${config.database}"
                    username = config.username
                    password = config.password

                    addDataSourceProperty("tcpKeepAlive", "true")
                }
                else -> throw IllegalArgumentException("Unsupported sql driver '${config.driver}'")
            }

            poolName = "varp"
        }

        // Create the connection pool.
        val dataSource = runCatching { HikariDataSource(hikariConfig) }
            .getOrElse { return Result.failure(it) }
        val handle = SqlStorageHandle(eventScope(), dataSource)

        // Check if the connection was successful.
        if (!handle.isConnected()) {
            throw IOException("Unable to connect to the database")
        }

        // Set up the database.
        Database.connect(dataSource)

        transaction {
            // Create all tables if they do not already exist.
            SchemaUtils.create(WarpTable, FolderTable)

            // Create a default root folder if it doesn't exist yet.
            val defaultRootState = FolderState.defaultRootState()
            FolderTable.insertIgnore { entry ->
                entry[path] = RootPath.toString()
                entry[name] = defaultRootState.name.originalMessage
                entry[description] = defaultRootState.description.joinToString("\n") { it.originalMessage }
                entry[tags] = defaultRootState.tags.joinToString(",")
                entry[properties] = defaultRootState.properties.map { "${it.key}=${it.value}" }.joinToString("\n")
            }
        }

        return@runCatching handle
    }

    override suspend fun close(config: SqlStorageConfig, handle: SqlStorageHandle): Result<Unit> = runCatching {
        // Close the database connection.
        handle.dataSource.close()
    }

    override suspend fun loadContent(
        config: SqlStorageConfig,
        handle: SqlStorageHandle,
    ): Result<TreeState> = runCatching {
        val treeState = MutableTreeState()
        transaction {
            FolderTable.selectAll().forEach { entry ->
                val path = NodeParentPath.parse(entry[FolderTable.path]).getOrThrow()
                val state = FolderState(
                    ComponentTemplate(entry[FolderTable.name]),
                    entry[FolderTable.description].split("\n").filter { it.isNotBlank() }.map { ComponentTemplate(it) },
                    entry[FolderTable.tags].split(",").filter { it.isNotBlank() }.toSet(),
                    entry[FolderTable.properties].split(",").filter { it.contains("=") }.associate {
                        val parts = it.split("=")
                        parts[0] to parts[1]
                    }
                )

                treeState[path] = state
            }

            WarpTable.selectAll().forEach { entry ->
                val path = WarpPath.parse(entry[WarpTable.path]).getOrThrow()
                val state = WarpState(
                    MinecraftLocation(
                        Key.key(entry[WarpTable.world]),
                        entry[WarpTable.x],
                        entry[WarpTable.y],
                        entry[WarpTable.z],
                        entry[WarpTable.yaw],
                        entry[WarpTable.pitch],
                    ),
                    ComponentTemplate(entry[WarpTable.name]),
                    entry[WarpTable.description].split("\n").filter { it.isNotBlank() }.map { ComponentTemplate(it) },
                    entry[WarpTable.tags].split(",").filter { it.isNotBlank() }.toSet(),
                    entry[WarpTable.properties].split(",").filter { it.contains("=") }.associate {
                        val parts = it.split("=")
                        parts[0] to parts[1]
                    }
                )

                treeState[path] = state
            }
        }
        return@runCatching treeState
    }

    override suspend fun createWarp(
        config: SqlStorageConfig,
        handle: SqlStorageHandle,
        path: WarpPath,
        state: WarpState,
    ): Result<Unit> = runCatching {
        transaction {
            WarpTable.insert { entry ->
                entry[WarpTable.path] = path.toString()
                entry[name] = state.name.originalMessage
                entry[description] = state.description.joinToString("\n") { it.originalMessage }
                entry[tags] = state.tags.joinToString(",")
                entry[properties] = state.properties.map { "${it.key}=${it.value}" }.joinToString("\n")
                entry[world] = state.location.world.toString()
                entry[x] = state.location.x
                entry[y] = state.location.y
                entry[z] = state.location.z
                entry[yaw] = state.location.yaw
                entry[pitch] = state.location.pitch
            }
        }
    }

    override suspend fun createFolder(
        config: SqlStorageConfig,
        handle: SqlStorageHandle,
        path: FolderPath,
        state: FolderState,
    ): Result<Unit> = runCatching {
        transaction {
            FolderTable.insert { entry ->
                entry[FolderTable.path] = path.toString()
                entry[name] = state.name.originalMessage
                entry[description] = state.description.joinToString("\n") { it.originalMessage }
                entry[tags] = state.tags.joinToString(",")
                entry[properties] = state.properties.map { "${it.key}=${it.value}" }.joinToString("\n")
            }
        }
    }

    override suspend fun saveWarp(
        config: SqlStorageConfig,
        handle: SqlStorageHandle,
        path: WarpPath,
        state: WarpState,
    ): Result<Unit> = runCatching {
        transaction {
            WarpTable.update({ WarpTable.path eq path.toString() }) { entry ->
                entry[name] = state.name.originalMessage
                entry[description] = state.description.joinToString("\n") { it.originalMessage }
                entry[tags] = state.tags.joinToString(",")
                entry[properties] = state.properties.map { "${it.key}=${it.value}" }.joinToString("\n")
                entry[world] = state.location.world.toString()
                entry[x] = state.location.x
                entry[y] = state.location.y
                entry[z] = state.location.z
                entry[yaw] = state.location.yaw
                entry[pitch] = state.location.pitch
            }
        }
    }

    override suspend fun saveFolder(
        config: SqlStorageConfig,
        handle: SqlStorageHandle,
        path: FolderPath,
        state: FolderState,
    ): Result<Unit> = runCatching {
        transaction {
            FolderTable.update({ FolderTable.path eq path.toString() }) { entry ->
                entry[name] = state.name.originalMessage
                entry[description] = state.description.joinToString("\n") { it.originalMessage }
                entry[tags] = state.tags.joinToString(",")
                entry[properties] = state.properties.map { "${it.key}=${it.value}" }.joinToString("\n")
            }
        }
    }

    override suspend fun saveRoot(
        config: SqlStorageConfig,
        handle: SqlStorageHandle,
        state: FolderState,
    ): Result<Unit> = runCatching {
        transaction {
            FolderTable.update({ FolderTable.path eq RootPath.toString() }) { entry ->
                entry[name] = state.name.originalMessage
                entry[description] = state.description.joinToString("\n") { it.originalMessage }
                entry[tags] = state.tags.joinToString(",")
                entry[properties] = state.properties.map { "${it.key}=${it.value}" }.joinToString("\n")
            }
        }
    }

    override suspend fun deleteWarp(
        config: SqlStorageConfig,
        handle: SqlStorageHandle,
        path: WarpPath,
    ): Result<Unit> = runCatching {
        transaction {
            WarpTable.deleteWhere { WarpTable.path eq path.toString() }
        }
    }

    override suspend fun deleteFolder(
        config: SqlStorageConfig,
        handle: SqlStorageHandle,
        path: FolderPath,
    ): Result<Unit> = runCatching {
        transaction {
            FolderTable.deleteWhere { FolderTable.path like "$path%" }
            WarpTable.deleteWhere { WarpTable.path like "$path%" }
        }
    }

    override suspend fun moveWarp(
        config: SqlStorageConfig,
        handle: SqlStorageHandle,
        src: WarpPath,
        dst: WarpPath,
    ): Result<Unit> = runCatching {
        transaction {
            WarpTable.update({ WarpTable.path eq src.toString() }) {
                it[path] = dst.toString()
            }
        }
    }

    override suspend fun moveFolder(
        config: SqlStorageConfig,
        handle: SqlStorageHandle,
        src: FolderPath,
        dst: FolderPath,
    ): Result<Unit> = runCatching {
        transaction {
            FolderTable.update({ FolderTable.path like "$src%" }) {
                it[path] = ReplaceFunction(path, src.toString(), dst.toString())
            }
            WarpTable.update({ WarpTable.path like "$src%" }) {
                it[path] = ReplaceFunction(path, src.toString(), dst.toString())
            }
        }
    }
}
