package net.voxelpi.varp.repository.mysql

import net.voxelpi.varp.warp.path.FolderPath
import net.voxelpi.varp.warp.path.WarpPath
import net.voxelpi.varp.warp.repository.Repository
import net.voxelpi.varp.warp.repository.RepositoryLoader
import net.voxelpi.varp.warp.repository.RepositoryType
import net.voxelpi.varp.warp.state.FolderState
import net.voxelpi.varp.warp.state.TreeStateRegistryView
import net.voxelpi.varp.warp.state.WarpState

@RepositoryType("mysql")
class MySQLRepository(
    override val id: String,
    val hostname: String,
    val port: Int,
    val database: String,
    val username: String,
    val password: String,
) : Repository {

    override val registry: TreeStateRegistryView
        get() = TODO("Not yet implemented")

    @RepositoryLoader
    public constructor(id: String, config: MySQLRepositoryConfig) : this(id, config.hostname, config.port, config.database, config.username, config.password)

    override fun reload(): Result<Unit> {
        TODO("Not yet implemented")
    }

    override fun createWarpState(path: WarpPath, state: WarpState): Result<Unit> {
        TODO("Not yet implemented")
    }

    override fun createFolderState(path: FolderPath, state: FolderState): Result<Unit> {
        TODO("Not yet implemented")
    }

    override fun saveWarpState(path: WarpPath, state: WarpState): Result<Unit> {
        TODO("Not yet implemented")
    }

    override fun saveFolderState(path: FolderPath, state: FolderState): Result<Unit> {
        TODO("Not yet implemented")
    }

    override fun saveRootState(state: FolderState): Result<Unit> {
        TODO("Not yet implemented")
    }

    override fun deleteWarpState(path: WarpPath): Result<Unit> {
        TODO("Not yet implemented")
    }

    override fun deleteFolderState(path: FolderPath): Result<Unit> {
        TODO("Not yet implemented")
    }

    override fun moveWarpState(src: WarpPath, dst: WarpPath): Result<Unit> {
        TODO("Not yet implemented")
    }

    override fun moveFolderState(src: FolderPath, dst: FolderPath): Result<Unit> {
        TODO("Not yet implemented")
    }
}
