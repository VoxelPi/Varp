package net.voxelpi.varp.warp.repository

import net.voxelpi.varp.exception.tree.FolderNotFoundException
import net.voxelpi.varp.exception.tree.WarpNotFoundException
import net.voxelpi.varp.warp.path.FolderPath
import net.voxelpi.varp.warp.path.WarpPath
import net.voxelpi.varp.warp.state.FolderState
import net.voxelpi.varp.warp.state.TreeStateRegistry
import net.voxelpi.varp.warp.state.TreeStateRegistryView
import net.voxelpi.varp.warp.state.WarpState

/**
 * The data source for a varp tree.
 * @property id The id of the repository.
 */
public abstract class SimpleRepository(
    public override val id: String,
) : Repository {

    protected val registry: TreeStateRegistry = TreeStateRegistry()

    public override val registryView: TreeStateRegistryView
        get() = registry

    // region content management

    protected abstract suspend fun handleLoad(): Result<Unit>

    protected abstract suspend fun handleCreate(path: WarpPath, state: WarpState): Result<Unit>

    protected abstract suspend fun handleCreate(path: FolderPath, state: FolderState): Result<Unit>

    protected abstract suspend fun handleSave(path: WarpPath, state: WarpState): Result<Unit>

    protected abstract suspend fun handleSave(path: FolderPath, state: FolderState): Result<Unit>

    protected abstract suspend fun handleSave(state: FolderState): Result<Unit>

    protected abstract suspend fun handleDelete(path: WarpPath): Result<Unit>

    protected abstract suspend fun handleDelete(path: FolderPath): Result<Unit>

    protected abstract suspend fun handleMove(src: WarpPath, dst: WarpPath): Result<Unit>

    protected abstract suspend fun handleMove(src: FolderPath, dst: FolderPath): Result<Unit>

    // endregion

    public override suspend fun load(): Result<Unit> {
        // Update registry.
        registry.clear()

        // Run implementation logic.
        return handleLoad()
    }

    public override suspend fun create(path: WarpPath, state: WarpState): Result<Unit> {
        // Run implementation logic.
        handleCreate(path, state).getOrElse { return Result.failure(it) }

        // Update registry.
        registry[path] = state

        return Result.success(Unit)
    }

    public override suspend fun create(path: FolderPath, state: FolderState): Result<Unit> {
        // Run implementation logic.
        handleCreate(path, state).getOrElse { return Result.failure(it) }

        // Update registry.
        registry[path] = state

        return Result.success(Unit)
    }

    public override suspend fun save(path: WarpPath, state: WarpState): Result<Unit> {
        // Run implementation logic.
        handleSave(path, state).getOrElse { return Result.failure(it) }

        // Update registry.
        registry[path] = state

        return Result.success(Unit)
    }

    public override suspend fun save(path: FolderPath, state: FolderState): Result<Unit> {
        // Run implementation logic.
        handleSave(path, state).getOrElse { return Result.failure(it) }

        // Update registry.
        registry[path] = state

        return Result.success(Unit)
    }

    public override suspend fun save(state: FolderState): Result<Unit> {
        // Run implementation logic.
        handleSave(state).getOrElse { return Result.failure(it) }

        // Update registry.
        registry.root = state

        return Result.success(Unit)
    }

    public override suspend fun delete(path: WarpPath): Result<Unit> {
        // Run implementation logic.
        handleDelete(path).getOrElse { return Result.failure(it) }

        // Update registry.
        registry.delete(path) ?: return Result.failure(WarpNotFoundException(path))

        return Result.success(Unit)
    }

    public override suspend fun delete(path: FolderPath): Result<Unit> {
        // Run implementation logic.
        handleDelete(path).getOrElse { return Result.failure(it) }

        // Update registry.
        registry.delete(path) ?: return Result.failure(FolderNotFoundException(path))

        return Result.success(Unit)
    }

    public override suspend fun move(src: WarpPath, dst: WarpPath): Result<Unit> {
        // Run implementation logic.
        handleMove(src, dst).getOrElse { return Result.failure(it) }

        // Update registry.
        registry.move(src, dst) ?: return Result.failure(WarpNotFoundException(src))

        return Result.success(Unit)
    }

    public override suspend fun move(src: FolderPath, dst: FolderPath): Result<Unit> {
        // Run implementation logic.
        handleMove(src, dst).getOrElse { return Result.failure(it) }

        // Update registry.
        registry.move(src, dst) ?: return Result.failure(FolderNotFoundException(src))

        return Result.success(Unit)
    }
}
