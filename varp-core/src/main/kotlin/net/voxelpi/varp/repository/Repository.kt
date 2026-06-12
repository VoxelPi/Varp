package net.voxelpi.varp.repository

import net.voxelpi.event.EventScope
import net.voxelpi.event.eventScope
import net.voxelpi.event.on
import net.voxelpi.event.post
import net.voxelpi.varp.event.folder.FolderCreateEvent
import net.voxelpi.varp.event.folder.FolderDeleteEvent
import net.voxelpi.varp.event.folder.FolderPathChangeEvent
import net.voxelpi.varp.event.folder.FolderPostDeleteEvent
import net.voxelpi.varp.event.folder.FolderStateChangeEvent
import net.voxelpi.varp.event.root.RootStateChangeEvent
import net.voxelpi.varp.event.tree.TreeUpdateEvent
import net.voxelpi.varp.event.warp.WarpCreateEvent
import net.voxelpi.varp.event.warp.WarpDeleteEvent
import net.voxelpi.varp.event.warp.WarpPathChangeEvent
import net.voxelpi.varp.event.warp.WarpPostDeleteEvent
import net.voxelpi.varp.event.warp.WarpStateChangeEvent
import net.voxelpi.varp.exception.tree.FolderAlreadyExistsException
import net.voxelpi.varp.exception.tree.FolderMoveIntoChildException
import net.voxelpi.varp.exception.tree.FolderNotFoundException
import net.voxelpi.varp.exception.tree.NodeParentNotFoundException
import net.voxelpi.varp.exception.tree.WarpAlreadyExistsException
import net.voxelpi.varp.exception.tree.WarpNotFoundException
import net.voxelpi.varp.tree.Folder
import net.voxelpi.varp.tree.Tree
import net.voxelpi.varp.tree.Warp
import net.voxelpi.varp.tree.path.FolderPath
import net.voxelpi.varp.tree.path.NodeParentPath
import net.voxelpi.varp.tree.path.RootPath
import net.voxelpi.varp.tree.path.WarpPath
import net.voxelpi.varp.tree.state.FolderState
import net.voxelpi.varp.tree.state.MutableTreeState
import net.voxelpi.varp.tree.state.TreeState
import net.voxelpi.varp.tree.state.WarpState

/**
 * A varp repository. This is the datasource for a varp tree.
 */
public class Repository<C : Any, H : StorageHandle>(
    public val id: String,
    public val storage: StorageInstance<C, H>,
) : Tree {

    public constructor(id: String, storage: Storage<C, H>, config: C) : this(id, StorageInstance(storage, config))

    public override val state: TreeState
        field = MutableTreeState()

    override val eventScope: EventScope = eventScope()

    init {
        storage.on { event: StorageEvents.TreeStateChangeEvent ->
            val previousState = state.copy()
            state.update(event.newState)
            eventScope.post(TreeUpdateEvent(this, previousState, state))
        }
        storage.on { event: StorageEvents.WarpCreateEvent ->
            state[event.path] = event.state
            eventScope.post(WarpCreateEvent(this[event.path]!!))
        }
        storage.on { event: StorageEvents.FolderCreateEvent ->
            state[event.path] = event.state
            eventScope.post(FolderCreateEvent(this[event.path]!!))
        }
        storage.on { event: StorageEvents.WarpStateChangeEvent ->
            val oldState = state[event.path] ?: return@on
            state[event.path] = event.newState
            eventScope.post(WarpStateChangeEvent(this[event.path]!!, newState = event.newState, oldState = oldState))
        }
        storage.on { event: StorageEvents.FolderStateChangeEvent ->
            val oldState = state[event.path] ?: return@on
            state[event.path] = event.newState
            eventScope.post(FolderStateChangeEvent(this[event.path]!!, newState = event.newState, oldState = oldState))
        }
        storage.on { event: StorageEvents.RootStateChangeEvent ->
            val oldState = state.root
            state.root = event.newState
            eventScope.post(RootStateChangeEvent(this.root, newState = event.newState, oldState = oldState))
        }
        storage.on { event: StorageEvents.WarpDeleteEvent ->
            eventScope.post(WarpDeleteEvent(this[event.path]!!))
            val oldState = state.delete(event.path) ?: return@on
            eventScope.post(WarpPostDeleteEvent(event.path, oldState))
        }
        storage.on { event: StorageEvents.FolderDeleteEvent ->
            eventScope.post(FolderDeleteEvent(this[event.path]!!))
            val oldState = state.delete(event.path) ?: return@on
            eventScope.post(FolderPostDeleteEvent(event.path, oldState))
        }
        storage.on { event: StorageEvents.WarpPathChangeEvent ->
            state.move(src = event.oldPath, dst = event.newPath)
            eventScope.post(WarpPathChangeEvent(this[event.newPath]!!, newPath = event.newPath, oldPath = event.oldPath))
        }
        storage.on { event: StorageEvents.FolderPathChangeEvent ->
            state.move(src = event.oldPath, dst = event.newPath)
            eventScope.post(FolderPathChangeEvent(this[event.newPath]!!, newPath = event.newPath, oldPath = event.oldPath))
        }
    }

    /**
     * Opens the storage instance of this repository.
     */
    public suspend fun open(): Result<Unit> = runCatching {
        storage.open().getOrThrow()
    }

    /**
     * Closes the storage instance of this repository.
     */
    public suspend fun close(): Result<Unit> = runCatching {
        storage.close().getOrThrow()
    }

    /**
     * Reloads the content of the repository.
     */
    public suspend fun load(): Result<Unit> = runCatching {
        storage.loadTree().getOrThrow()
    }

    public suspend fun update(path: NodeParentPath, newState: TreeState): Result<Unit> = runCatching {
        // Check that the parent exists.
        if (path is FolderPath && path.parent !in this) {
            throw NodeParentNotFoundException(path.parent)
        }

        storage.updateTree(path, newState).getOrThrow()
    }

    public override suspend fun create(path: WarpPath, state: WarpState): Result<Warp> = runCatching {
        // Check that a warp doesn't already exist at that path.
        if (path in this) {
            throw WarpAlreadyExistsException(path)
        }

        // Check that the parent exists.
        if (path.parent !in this) {
            throw NodeParentNotFoundException(path.parent)
        }

        // Update storage.
        storage.createWarp(path, state).getOrThrow()

        return@runCatching this[path]!!
    }

    public override suspend fun create(path: FolderPath, state: FolderState): Result<Folder> = runCatching {
        // Check that a folder doesn't already exist at that path.
        if (path in this) {
            throw FolderAlreadyExistsException(path)
        }

        // Check if the parent exists.
        if (path.parent !in this) {
            throw NodeParentNotFoundException(path.parent)
        }

        // Update storage.
        storage.createFolder(path, state).getOrThrow()

        return@runCatching this[path]!!
    }

    public override suspend fun create(path: FolderPath, state: TreeState): Result<Folder> = runCatching {
        // Check that a folder doesn't already exist at that path.
        if (path in this) {
            throw FolderAlreadyExistsException(path)
        }

        // Check if the parent exists.
        if (path.parent !in this) {
            throw NodeParentNotFoundException(path.parent)
        }

        // Update storage.
        storage.updateTree(path, state).getOrThrow()

        return@runCatching this[path]!!
    }

    public override suspend fun update(path: WarpPath, newState: WarpState): Result<Unit> = runCatching {
        // Check that a warp exists at the given path.
        if (path !in this) {
            throw WarpNotFoundException(path)
        }

        // Update storage.
        storage.updateWarp(path, newState).getOrThrow()
    }

    public override suspend fun update(path: FolderPath, newState: FolderState): Result<Unit> = runCatching {
        // Check that a folder exists at the given path.
        if (path !in this) {
            throw FolderNotFoundException(path)
        }

        // Update storage.
        storage.updateFolder(path, newState).getOrThrow()
    }

    public override suspend fun update(path: RootPath, newState: FolderState): Result<Unit> = runCatching {
        // Update storage.
        storage.updateRoot(newState).getOrThrow()
    }

    public override suspend fun delete(path: WarpPath): Result<WarpState> = runCatching {
        // Check that a warp exists at the given path.
        val previousState = this.state[path] ?: run {
            throw WarpNotFoundException(path)
        }

        // Update storage.
        storage.deleteWarp(path).getOrThrow()

        // Return the previous state.
        return@runCatching previousState
    }

    public override suspend fun delete(path: FolderPath): Result<FolderState> = runCatching {
        // Check that a folder exists at the given path.
        val previousState = this.state[path] ?: run {
            throw FolderNotFoundException(path)
        }

        // Update storage.
        storage.deleteFolder(path).getOrThrow()

        // Return the previous state.
        return@runCatching previousState
    }

    public override suspend fun move(
        src: WarpPath,
        dst: WarpPath,
    ): Result<Unit> = runCatching {
        // Check that the given warp exists.
        if (src !in this) {
            throw WarpNotFoundException(src)
        }

        // Early exit if the src and destination paths are the same.
        if (src == dst) {
            return Result.success(Unit)
        }

        // Fail if a warp already exists at the destination path.
        if (dst in this) {
            throw WarpAlreadyExistsException(dst)
        }

        // Update storage.
        storage.moveWarp(src, dst)
    }

    public override suspend fun move(
        src: FolderPath,
        dst: FolderPath,
    ): Result<Unit> = runCatching {
        // Check that the given folder exists.
        if (src !in this) {
            throw FolderNotFoundException(src)
        }

        // Early exit if the src and destination paths are the same.
        if (src == dst) {
            return Result.success(Unit)
        }

        // Fail if the destination is a subpath of the source.
        if (dst.isSubpathOf(src)) {
            throw FolderMoveIntoChildException(src, dst)
        }

        // Fail if a folder already exists at the destination path.
        if (dst in this) {
            throw FolderAlreadyExistsException(dst)
        }

        // Update storage.
        storage.moveFolder(src, dst)
    }
}
