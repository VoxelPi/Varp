package net.voxelpi.varp.repository.mysql

import net.voxelpi.varp.warp.path.FolderPath
import net.voxelpi.varp.warp.path.WarpPath
import net.voxelpi.varp.warp.repository.RepositoryLoader
import net.voxelpi.varp.warp.repository.RepositoryType
import net.voxelpi.varp.warp.repository.SimpleRepository
import net.voxelpi.varp.warp.state.FolderState
import net.voxelpi.varp.warp.state.WarpState

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
        return super.activate()
    }

    override suspend fun deactivate(): Result<Unit> {
        return super.deactivate()
    }

    override suspend fun handleLoad(): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun handleCreate(path: WarpPath, state: WarpState): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun handleCreate(path: FolderPath, state: FolderState): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun handleSave(path: WarpPath, state: WarpState): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun handleSave(path: FolderPath, state: FolderState): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun handleSave(state: FolderState): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun handleDelete(path: WarpPath): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun handleDelete(path: FolderPath): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun handleMove(src: WarpPath, dst: WarpPath): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun handleMove(src: FolderPath, dst: FolderPath): Result<Unit> {
        TODO("Not yet implemented")
    }
}
