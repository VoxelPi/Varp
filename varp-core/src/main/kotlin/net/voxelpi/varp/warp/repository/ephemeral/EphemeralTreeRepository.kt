package net.voxelpi.varp.warp.repository.ephemeral

import net.voxelpi.varp.exception.tree.FolderNotFoundException
import net.voxelpi.varp.exception.tree.WarpNotFoundException
import net.voxelpi.varp.warp.path.FolderPath
import net.voxelpi.varp.warp.path.WarpPath
import net.voxelpi.varp.warp.repository.TreeRepository
import net.voxelpi.varp.warp.state.FolderState
import net.voxelpi.varp.warp.state.TreeStateRegistry
import net.voxelpi.varp.warp.state.WarpState

public class EphemeralTreeRepository(
    override val id: String,
) : TreeRepository {

    override val registry: TreeStateRegistry = TreeStateRegistry()

    override fun reload(): Result<Unit> {
        registry.clear()
        return Result.success(Unit)
    }

    override fun createWarpState(path: WarpPath, state: WarpState): Result<Unit> {
        registry[path] = state
        return Result.success(Unit)
    }

    override fun createFolderState(path: FolderPath, state: FolderState): Result<Unit> {
        registry[path] = state
        return Result.success(Unit)
    }

    override fun saveWarpState(path: WarpPath, state: WarpState): Result<Unit> {
        registry[path] = state
        return Result.success(Unit)
    }

    override fun saveFolderState(path: FolderPath, state: FolderState): Result<Unit> {
        registry[path] = state
        return Result.success(Unit)
    }

    override fun saveRootState(state: FolderState): Result<Unit> {
        registry.root = state
        return Result.success(Unit)
    }

    override fun deleteWarpState(path: WarpPath): Result<Unit> {
        registry.remove(path) ?: return Result.failure(WarpNotFoundException(path))
        return Result.success(Unit)
    }

    override fun deleteFolderState(path: FolderPath): Result<Unit> {
        registry.remove(path) ?: return Result.failure(FolderNotFoundException(path))
        return Result.success(Unit)
    }

    override fun moveWarpState(src: WarpPath, dst: WarpPath): Result<Unit> {
        registry.move(src, dst) ?: return Result.failure(WarpNotFoundException(src))
        return Result.success(Unit)
    }

    override fun moveFolderState(src: FolderPath, dst: FolderPath): Result<Unit> {
        registry.move(src, dst) ?: return Result.failure(FolderNotFoundException(src))
        return Result.success(Unit)
    }
}
