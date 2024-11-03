package net.voxelpi.varp.warp.repository

import net.voxelpi.event.post
import net.voxelpi.varp.event.folder.FolderCreateEvent
import net.voxelpi.varp.event.folder.FolderDeleteEvent
import net.voxelpi.varp.event.folder.FolderPathChangeEvent
import net.voxelpi.varp.event.folder.FolderPostDeleteEvent
import net.voxelpi.varp.event.folder.FolderStateChangeEvent
import net.voxelpi.varp.event.repository.RepositoryLoadEvent
import net.voxelpi.varp.event.root.RootStateChangeEvent
import net.voxelpi.varp.event.warp.WarpCreateEvent
import net.voxelpi.varp.event.warp.WarpDeleteEvent
import net.voxelpi.varp.event.warp.WarpPathChangeEvent
import net.voxelpi.varp.event.warp.WarpPostDeleteEvent
import net.voxelpi.varp.event.warp.WarpStateChangeEvent
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
    id: String,
) : Repository(id) {

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
        handleLoad().onFailure { return Result.failure(it) }

        // Post the repository load event.
        tree.eventScope.post(RepositoryLoadEvent(this))

        return Result.success(Unit)
    }

    public override suspend fun create(path: WarpPath, state: WarpState): Result<Unit> {
        // Run implementation logic.
        handleCreate(path, state).getOrElse { return Result.failure(it) }

        // Update registry.
        registry[path] = state

        // Post event.
        tree.eventScope.post(WarpCreateEvent(tree.resolve(path)!!))

        return Result.success(Unit)
    }

    public override suspend fun create(path: FolderPath, state: FolderState): Result<Unit> {
        // Run implementation logic.
        handleCreate(path, state).getOrElse { return Result.failure(it) }

        // Update registry.
        registry[path] = state

        // Post event.
        tree.eventScope.post(FolderCreateEvent(tree.resolve(path)!!))

        return Result.success(Unit)
    }

    public override suspend fun save(path: WarpPath, state: WarpState): Result<Unit> {
        // Temporary save previous state.
        val previousState = registry[path] ?: run {
            return Result.failure(WarpNotFoundException(path))
        }

        // Run implementation logic.
        handleSave(path, state).getOrElse { return Result.failure(it) }

        // Update registry.
        registry[path] = state

        // Post event.
        tree.eventScope.post(WarpStateChangeEvent(tree.resolve(path)!!, state, previousState))

        return Result.success(Unit)
    }

    public override suspend fun save(path: FolderPath, state: FolderState): Result<Unit> {
        // Temporary save previous state.
        val previousState = registry[path] ?: run {
            return Result.failure(FolderNotFoundException(path))
        }

        // Run implementation logic.
        handleSave(path, state).getOrElse { return Result.failure(it) }

        // Update registry.
        registry[path] = state

        // Post event.
        tree.eventScope.post(FolderStateChangeEvent(tree.resolve(path)!!, state, previousState))

        return Result.success(Unit)
    }

    public override suspend fun save(state: FolderState): Result<Unit> {
        // Check if a module exists at the given path.
        val previousState = registry.root

        // Run implementation logic.
        handleSave(state).getOrElse { return Result.failure(it) }

        // Update registry.
        registry.root = state

        // Post event.
        tree.eventScope.post(RootStateChangeEvent(tree.root, state, previousState))

        return Result.success(Unit)
    }

    public override suspend fun delete(path: WarpPath): Result<Unit> {
        val warp = tree.resolve(path) ?: return Result.failure(WarpNotFoundException(path))

        // Post event.
        tree.eventScope.post(WarpDeleteEvent(warp))

        // Run implementation logic.
        handleDelete(path).getOrElse { return Result.failure(it) }

        // Update registry.
        val state = registry.delete(path) ?: return Result.failure(WarpNotFoundException(path))

        // Post event.
        tree.eventScope.post(WarpPostDeleteEvent(path, state))

        return Result.success(Unit)
    }

    public override suspend fun delete(path: FolderPath): Result<Unit> {
        val folder = tree.resolve(path) ?: return Result.failure(FolderNotFoundException(path))

        // Post event.
        tree.eventScope.post(FolderDeleteEvent(folder))

        // Run implementation logic.
        handleDelete(path).getOrElse { return Result.failure(it) }

        // Update registry.
        val state = registry.delete(path) ?: return Result.failure(FolderNotFoundException(path))

        // Post event.
        tree.eventScope.post(FolderPostDeleteEvent(path, state))

        return Result.success(Unit)
    }

    public override suspend fun move(src: WarpPath, dst: WarpPath): Result<Unit> {
        // Run implementation logic.
        handleMove(src, dst).getOrElse { return Result.failure(it) }

        // Update registry.
        registry.move(src, dst) ?: return Result.failure(WarpNotFoundException(src))

        // Post event.
        tree.eventScope.post(WarpPathChangeEvent(tree.resolve(dst)!!, dst, src))

        return Result.success(Unit)
    }

    public override suspend fun move(src: FolderPath, dst: FolderPath): Result<Unit> {
        // Run implementation logic.
        handleMove(src, dst).getOrElse { return Result.failure(it) }

        // Update registry.
        registry.move(src, dst) ?: return Result.failure(FolderNotFoundException(src))

        // Post event.
        tree.eventScope.post(FolderPathChangeEvent(tree.resolve(dst)!!, dst, src))

        return Result.success(Unit)
    }
}
