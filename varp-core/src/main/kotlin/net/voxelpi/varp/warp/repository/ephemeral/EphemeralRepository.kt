package net.voxelpi.varp.warp.repository.ephemeral

import net.voxelpi.varp.warp.path.FolderPath
import net.voxelpi.varp.warp.path.WarpPath
import net.voxelpi.varp.warp.repository.SimpleRepository
import net.voxelpi.varp.warp.state.FolderState
import net.voxelpi.varp.warp.state.WarpState

public class EphemeralRepository(
    id: String,
    override val config: EphemeralRepositoryConfig = EphemeralRepositoryConfig,
) : SimpleRepository(id) {

    override val type: EphemeralRepositoryType
        get() = EphemeralRepositoryType

    override suspend fun handleLoad(): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun handleCreate(path: WarpPath, state: WarpState): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun handleCreate(path: FolderPath, state: FolderState): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun handleSave(path: WarpPath, state: WarpState): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun handleSave(path: FolderPath, state: FolderState): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun handleSave(state: FolderState): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun handleDelete(path: WarpPath): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun handleDelete(path: FolderPath): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun handleMove(src: WarpPath, dst: WarpPath): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun handleMove(src: FolderPath, dst: FolderPath): Result<Unit> {
        return Result.success(Unit)
    }
}
