package net.voxelpi.varp.repository.mysql

import net.kyori.adventure.key.Key
import net.kyori.adventure.text.minimessage.MiniMessage.miniMessage
import net.voxelpi.varp.MinecraftLocation
import net.voxelpi.varp.repository.mysql.functions.ReplaceFunction
import net.voxelpi.varp.repository.mysql.tables.Folders
import net.voxelpi.varp.repository.mysql.tables.Warps
import net.voxelpi.varp.warp.path.FolderPath
import net.voxelpi.varp.warp.path.NodeParentPath
import net.voxelpi.varp.warp.path.RootPath
import net.voxelpi.varp.warp.path.WarpPath
import net.voxelpi.varp.warp.repository.RepositoryLoader
import net.voxelpi.varp.warp.repository.RepositoryType
import net.voxelpi.varp.warp.repository.SimpleRepository
import net.voxelpi.varp.warp.state.FolderState
import net.voxelpi.varp.warp.state.WarpState
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

@RepositoryType("mysql")
class MySQLRepository(
    id: String,
    val hostname: String,
    val port: Int,
    val database: String,
    val username: String,
    val password: String,
) : SimpleRepository(id) {

    @RepositoryLoader
    public constructor(id: String, config: MySQLRepositoryConfig) : this(id, config.hostname, config.port, config.database, config.username, config.password)

    override suspend fun activate(): Result<Unit> {
        runCatching {
            Database.connect(
                "jdbc:mysql://$hostname:$port/$database",
                driver = "com.mysql.cj.jdbc.Driver",
                user = username,
                password = password,
            )

            transaction {
                // Create tables.
                SchemaUtils.create(Warps)
                SchemaUtils.create(Folders)

                // Create root folder data if doesn't already exist.
                val rootExists = Folders.selectAll().where { Folders.path eq RootPath.toString() }.count() > 0
                if (!rootExists) {
                    Folders.insert {
                        val state = FolderState.defaultRootState()
                        it[path] = RootPath.toString()
                        it[name] = miniMessage().serialize(state.name)
                        it[description] = state.description.map { miniMessage().serialize(it) }.joinToString("\n")
                        it[tags] = state.tags.joinToString(",")
                        it[properties] = state.properties.map { "${it.key}=${it.value}" }.joinToString("\n")
                    }
                }
            }
        }

        return super.activate()
    }

    override suspend fun deactivate(): Result<Unit> {
        return super.deactivate()
    }

    override suspend fun handleLoad(): Result<Unit> {
        return runCatching {
            transaction {
                Folders.selectAll().forEach {
                    val path = NodeParentPath.parse(it[Folders.path]).getOrThrow()
                    val state = FolderState(
                        miniMessage().deserialize(it[Folders.name]),
                        it[Folders.description].split("\n").filter { it.isNotBlank() }.map { miniMessage().deserialize(it) },
                        it[Folders.tags].split(",").filter { it.isNotBlank() }.toSet(),
                        it[Folders.properties].split(",").filter { it.contains("=") }.map {
                            val parts = it.split("=")
                            parts[0] to parts[1]
                        }.toMap()
                    )

                    registry[path] = state
                }

                Warps.selectAll().forEach {
                    val path = WarpPath.parse(it[Warps.path]).getOrThrow()
                    val state = WarpState(
                        MinecraftLocation(Key.key(it[Warps.world]), it[Warps.x], it[Warps.y], it[Warps.z], it[Warps.yaw], it[Warps.pitch]),
                        miniMessage().deserialize(it[Warps.name]),
                        it[Warps.description].split("\n").filter { it.isNotBlank() }.map { miniMessage().deserialize(it) },
                        it[Warps.tags].split(",").filter { it.isNotBlank() }.toSet(),
                        it[Warps.properties].split(",").filter { it.contains("=") }.map {
                            val parts = it.split("=")
                            parts[0] to parts[1]
                        }.toMap()
                    )

                    registry[path] = state
                }
            }
        }
    }

    override suspend fun handleCreate(path: WarpPath, state: WarpState): Result<Unit> {
        return runCatching {
            transaction {
                Warps.insert {
                    it[Warps.path] = path.toString()
                    it[name] = miniMessage().serialize(state.name)
                    it[description] = state.description.map { miniMessage().serialize(it) }.joinToString("\n")
                    it[tags] = state.tags.joinToString(",")
                    it[properties] = state.properties.map { "${it.key}=${it.value}" }.joinToString("\n")
                    it[world] = state.location.world.toString()
                    it[x] = state.location.x
                    it[y] = state.location.y
                    it[z] = state.location.z
                    it[yaw] = state.location.yaw
                    it[pitch] = state.location.pitch
                }
            }
        }
    }

    override suspend fun handleCreate(path: FolderPath, state: FolderState): Result<Unit> {
        return runCatching {
            transaction {
                Folders.insert {
                    it[Folders.path] = path.toString()
                    it[name] = miniMessage().serialize(state.name)
                    it[description] = state.description.map { miniMessage().serialize(it) }.joinToString("\n")
                    it[tags] = state.tags.joinToString(",")
                    it[properties] = state.properties.map { "${it.key}=${it.value}" }.joinToString("\n")
                }
            }
        }
    }

    override suspend fun handleSave(path: WarpPath, state: WarpState): Result<Unit> {
        return runCatching {
            transaction {
                Warps.update({ Warps.path eq path.toString() }) {
                    it[name] = miniMessage().serialize(state.name)
                    it[description] = state.description.map { miniMessage().serialize(it) }.joinToString("\n")
                    it[tags] = state.tags.joinToString(",")
                    it[properties] = state.properties.map { "${it.key}=${it.value}" }.joinToString("\n")
                    it[world] = state.location.world.toString()
                    it[x] = state.location.x
                    it[y] = state.location.y
                    it[z] = state.location.z
                    it[yaw] = state.location.yaw
                    it[pitch] = state.location.pitch
                }
            }
        }
    }

    override suspend fun handleSave(path: FolderPath, state: FolderState): Result<Unit> {
        return runCatching {
            transaction {
                Folders.update({ Folders.path eq path.toString() }) {
                    it[name] = miniMessage().serialize(state.name)
                    it[description] = state.description.map { miniMessage().serialize(it) }.joinToString("\n")
                    it[tags] = state.tags.joinToString(",")
                    it[properties] = state.properties.map { "${it.key}=${it.value}" }.joinToString("\n")
                }
            }
        }
    }

    override suspend fun handleSave(state: FolderState): Result<Unit> {
        return runCatching {
            transaction {
                Folders.update({ Folders.path eq RootPath.toString() }) {
                    it[name] = miniMessage().serialize(state.name)
                    it[description] = state.description.map { miniMessage().serialize(it) }.joinToString("\n")
                    it[tags] = state.tags.joinToString(",")
                    it[properties] = state.properties.map { "${it.key}=${it.value}" }.joinToString("\n")
                }
            }
        }
    }

    override suspend fun handleDelete(path: WarpPath): Result<Unit> {
        return runCatching {
            transaction {
                Warps.deleteWhere { Warps.path eq path.toString() }
            }
        }
    }

    override suspend fun handleDelete(path: FolderPath): Result<Unit> {
        return runCatching {
            transaction {
                Folders.deleteWhere { Folders.path eq path.toString() }
            }
        }
    }

    override suspend fun handleMove(src: WarpPath, dst: WarpPath): Result<Unit> {
        return runCatching {
            transaction {
                Warps.update({ Warps.path eq src.toString() }) {
                    it[path] = dst.toString()
                }
            }
        }
    }

    override suspend fun handleMove(src: FolderPath, dst: FolderPath): Result<Unit> {
        return runCatching {
            transaction {
                Folders.update({ Folders.path like "$src%" }) {
                    it[path] = ReplaceFunction(path, src.toString(), dst.toString())
                }
                Warps.update({ Warps.path like "$src%" }) {
                    it[path] = ReplaceFunction(path, src.toString(), dst.toString())
                }
            }
        }
    }
}
