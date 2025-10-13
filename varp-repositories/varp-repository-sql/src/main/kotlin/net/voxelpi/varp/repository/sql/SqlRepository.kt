package net.voxelpi.varp.repository.sql

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.minimessage.MiniMessage.miniMessage
import net.voxelpi.varp.MinecraftLocation
import net.voxelpi.varp.option.OptionsContext
import net.voxelpi.varp.repository.sql.function.ReplaceFunction
import net.voxelpi.varp.repository.sql.table.FolderTable
import net.voxelpi.varp.repository.sql.table.WarpTable
import net.voxelpi.varp.warp.path.FolderPath
import net.voxelpi.varp.warp.path.NodeParentPath
import net.voxelpi.varp.warp.path.RootPath
import net.voxelpi.varp.warp.path.WarpPath
import net.voxelpi.varp.warp.repository.RepositoryConfig
import net.voxelpi.varp.warp.repository.SimpleRepository
import net.voxelpi.varp.warp.state.FolderState
import net.voxelpi.varp.warp.state.WarpState
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

class SqlRepository(
    id: String,
    override val config: RepositoryConfig,
    override val type: SqlRepositoryType,
    private val hikariConfig: HikariConfig,
) : SimpleRepository(id) {

    private var dataSource: HikariDataSource? = null

    override suspend fun activate(): Result<Unit> {
        // Create the connection pool.
        val dataSource = runCatching { HikariDataSource(hikariConfig) }
            .getOrElse { return Result.failure(it) }

        this.dataSource = dataSource

        // Check if the connection was successful.
        if (!isConnected()) {
            return Result.failure(IOException("Unable to connect to the database"))
        }

        // Set up the database.
        try {
            Database.connect(dataSource)

            transaction {
                // Create all tables if they do not already exist.
                SchemaUtils.create(WarpTable, FolderTable)

                // Create a default root folder if it doesn't exist yet.
                val defaultRootState = FolderState.defaultRootState()
                FolderTable.insertIgnore { entry ->
                    entry[path] = RootPath.toString()
                    entry[name] = miniMessage().serialize(defaultRootState.name)
                    entry[description] = defaultRootState.description.joinToString("\n") { miniMessage().serialize(it) }
                    entry[tags] = defaultRootState.tags.joinToString(",")
                    entry[properties] = defaultRootState.properties.map { "${it.key}=${it.value}" }.joinToString("\n")
                }
            }
        } catch (exception: Exception) {
            return Result.failure(exception)
        }

        // Run super logic.
        return super.activate()
    }

    override suspend fun deactivate(): Result<Unit> {
        // Close the database connection.
        dataSource?.close()

        // Run super logic.
        return super.deactivate()
    }

    /**
     * Check if the repository is currently connected to the database.
     */
    fun isConnected(): Boolean {
        val dataSource = this.dataSource ?: return false

        dataSource.connection.use { connection ->
            return connection.isValid(5)
        }
    }

    override suspend fun handleLoad(): Result<Unit> = runCatching {
        transaction {
            FolderTable.selectAll().forEach { entry ->
                val path = NodeParentPath.parse(entry[FolderTable.path]).getOrThrow()
                val state = FolderState(
                    miniMessage().deserialize(entry[FolderTable.name]),
                    entry[FolderTable.description].split("\n").filter { it.isNotBlank() }.map { miniMessage().deserialize(it) },
                    entry[FolderTable.tags].split(",").filter { it.isNotBlank() }.toSet(),
                    entry[FolderTable.properties].split(",").filter { it.contains("=") }.associate {
                        val parts = it.split("=")
                        parts[0] to parts[1]
                    }
                )

                registry[path] = state
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
                    miniMessage().deserialize(entry[WarpTable.name]),
                    entry[WarpTable.description].split("\n").filter { it.isNotBlank() }.map { miniMessage().deserialize(it) },
                    entry[WarpTable.tags].split(",").filter { it.isNotBlank() }.toSet(),
                    entry[WarpTable.properties].split(",").filter { it.contains("=") }.associate {
                        val parts = it.split("=")
                        parts[0] to parts[1]
                    }
                )

                registry[path] = state
            }
        }
    }

    override suspend fun handleCreate(path: WarpPath, state: WarpState): Result<Unit> = runCatching {
        transaction {
            WarpTable.insert { entry ->
                entry[WarpTable.path] = path.toString()
                entry[name] = miniMessage().serialize(state.name)
                entry[description] = state.description.joinToString("\n") { miniMessage().serialize(it) }
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

    override suspend fun handleCreate(path: FolderPath, state: FolderState): Result<Unit> = runCatching {
        transaction {
            FolderTable.insert { entry ->
                entry[FolderTable.path] = path.toString()
                entry[name] = miniMessage().serialize(state.name)
                entry[description] = state.description.joinToString("\n") { miniMessage().serialize(it) }
                entry[tags] = state.tags.joinToString(",")
                entry[properties] = state.properties.map { "${it.key}=${it.value}" }.joinToString("\n")
            }
        }
    }

    override suspend fun handleSave(path: WarpPath, state: WarpState): Result<Unit> = runCatching {
        transaction {
            WarpTable.update({ WarpTable.path eq path.toString() }) { entry ->
                entry[name] = miniMessage().serialize(state.name)
                entry[description] = state.description.joinToString("\n") { miniMessage().serialize(it) }
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

    override suspend fun handleSave(path: FolderPath, state: FolderState): Result<Unit> = runCatching {
        transaction {
            FolderTable.update({ FolderTable.path eq path.toString() }) { entry ->
                entry[name] = miniMessage().serialize(state.name)
                entry[description] = state.description.joinToString("\n") { miniMessage().serialize(it) }
                entry[tags] = state.tags.joinToString(",")
                entry[properties] = state.properties.map { "${it.key}=${it.value}" }.joinToString("\n")
            }
        }
    }

    override suspend fun handleSave(state: FolderState): Result<Unit> = runCatching {
        transaction {
            FolderTable.update({ FolderTable.path eq RootPath.toString() }) { entry ->
                entry[name] = miniMessage().serialize(state.name)
                entry[description] = state.description.joinToString("\n") { miniMessage().serialize(it) }
                entry[tags] = state.tags.joinToString(",")
                entry[properties] = state.properties.map { "${it.key}=${it.value}" }.joinToString("\n")
            }
        }
    }

    override suspend fun handleDelete(path: WarpPath): Result<Unit> = runCatching {
        transaction {
            WarpTable.deleteWhere { WarpTable.path eq path.toString() }
        }
    }

    override suspend fun handleDelete(path: FolderPath): Result<Unit> = runCatching {
        transaction {
            FolderTable.deleteWhere { FolderTable.path like "$path%" }
            WarpTable.deleteWhere { WarpTable.path like "$path%" }
        }
    }

    override suspend fun handleMove(src: WarpPath, dst: WarpPath, options: OptionsContext): Result<Unit> = runCatching {
        transaction {
            WarpTable.update({ WarpTable.path eq src.toString() }) {
                it[path] = dst.toString()
            }
        }
    }

    override suspend fun handleMove(src: FolderPath, dst: FolderPath, options: OptionsContext): Result<Unit> = runCatching {
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
