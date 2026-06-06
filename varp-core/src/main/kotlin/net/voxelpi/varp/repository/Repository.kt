package net.voxelpi.varp.repository

import net.voxelpi.event.EventScope
import net.voxelpi.event.eventScope
import net.voxelpi.event.post
import net.voxelpi.varp.DuplicatesStrategy
import net.voxelpi.varp.event.folder.FolderCreateEvent
import net.voxelpi.varp.event.folder.FolderDeleteEvent
import net.voxelpi.varp.event.folder.FolderPostDeleteEvent
import net.voxelpi.varp.event.folder.FolderStateChangeEvent
import net.voxelpi.varp.event.repository.RepositoryLoadEvent
import net.voxelpi.varp.event.root.RootStateChangeEvent
import net.voxelpi.varp.event.warp.WarpCreateEvent
import net.voxelpi.varp.event.warp.WarpDeleteEvent
import net.voxelpi.varp.event.warp.WarpPostDeleteEvent
import net.voxelpi.varp.event.warp.WarpStateChangeEvent
import net.voxelpi.varp.exception.tree.FolderAlreadyExistsException
import net.voxelpi.varp.exception.tree.FolderMoveIntoChildException
import net.voxelpi.varp.exception.tree.FolderNotFoundException
import net.voxelpi.varp.exception.tree.NodeParentAlreadyExistsException
import net.voxelpi.varp.exception.tree.NodeParentNotFoundException
import net.voxelpi.varp.exception.tree.WarpAlreadyExistsException
import net.voxelpi.varp.exception.tree.WarpNotFoundException
import net.voxelpi.varp.tree.Folder
import net.voxelpi.varp.tree.Root
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

    /**
     * The storage handle of this repository. Is null if the repository is currently closed.
     */
    private var handle: H? = null

    private fun handleOrThrow(): H {
        return handle ?: throw IllegalStateException("Storage is closed for repository '$id'")
    }

    public override val state: TreeState
        field = MutableTreeState()

    override val eventScope: EventScope = eventScope()

    /**
     * Opens the storage instance of this repository.
     */
    internal suspend fun open(): Result<Unit> = runCatching {
        this.handle = storage.storage.open(storage.config).getOrThrow()
    }

    /**
     * Closes the storage instance of this repository.
     */
    internal suspend fun close(): Result<Unit> = runCatching {
        val handle = this.handle ?: run {
            throw IllegalStateException("Storage is already closed for repository '$id'")
        }
        storage.storage.close(storage.config, handle)
        this.handle = null
    }

    /**
     * Returns if the storage of this repository is currently open, meaning that it is ready to load / store data.
     */
    public val isOpen: Boolean
        get() = handle != null

    /**
     * Reloads the content of the repository.
     * The implementation should also post a [net.voxelpi.varp.event.repository.RepositoryLoadEvent] to the tree event bus.
     */
    public suspend fun load(): Result<Unit> = runCatching {
        val handle = handleOrThrow()
        val newState = storage.loadContent(handle).getOrThrow()

        // Update state.
        state.update(newState)

        // Post load event.
        eventScope.post(RepositoryLoadEvent(this))
    }

    public fun update(newState: TreeState) {
        state.update(newState)
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
        val handle = handleOrThrow()
        storage.createWarp(handle, path, state).getOrThrow()

        // Update state.
        this.state[path] = state
        val warp = Warp(this, path)

        // Post event.
        eventScope.post(WarpCreateEvent(warp))

        return@runCatching warp
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
        val handle = handleOrThrow()
        storage.createFolder(handle, path, state).getOrThrow()

        // Update state.
        this.state[path] = state
        val folder = Folder(this, path)

        // Post event.
        eventScope.post(FolderCreateEvent(folder))

        return@runCatching folder
    }

    public override suspend fun update(path: WarpPath, newState: WarpState): Result<WarpState> = runCatching {
        // Check that a warp exists at the given path.
        val previousState = state[path] ?: run {
            throw WarpNotFoundException(path)
        }

        // Update storage.
        val handle = handleOrThrow()
        storage.saveWarp(handle, path, newState).getOrThrow()

        // Update state.
        this.state[path] = newState
        val warp = Warp(this, path)

        // Post event.
        eventScope.post(WarpStateChangeEvent(warp, newState, previousState))

        newState
    }

    public override suspend fun update(path: FolderPath, newState: FolderState): Result<FolderState> = runCatching {
        // Check that a folder exists at the given path.
        val previousState = state[path] ?: run {
            throw FolderNotFoundException(path)
        }

        // Update storage.
        val handle = handleOrThrow()
        storage.saveFolder(handle, path, newState).getOrThrow()

        // Update state.
        this.state[path] = newState
        val folder = Folder(this, path)

        // Post event.
        eventScope.post(FolderStateChangeEvent(folder, newState, previousState))

        newState
    }

    public override suspend fun update(path: RootPath, newState: FolderState): Result<FolderState> = runCatching {
        val previousState = state.root

        // Update storage.
        val handle = handleOrThrow()
        storage.saveRoot(handle, newState).getOrThrow()

        // Update state.
        this.state[path] = newState
        val root = Root(this)

        // Post event.
        eventScope.post(RootStateChangeEvent(root, newState, previousState))

        newState
    }

    public override suspend fun delete(path: WarpPath): Result<WarpState> = runCatching {
        // Check that a warp exists at the given path.
        val previousState = this.state[path] ?: run {
            throw WarpNotFoundException(path)
        }
        val warp = Warp(this, path)

        // Post event.
        eventScope.post(WarpDeleteEvent(warp))

        // Update storage.
        val handle = handleOrThrow()
        storage.deleteWarp(handle, path).getOrThrow()

        // Update state.
        this.state.delete(path)

        // Post event.
        eventScope.post(WarpPostDeleteEvent(path, previousState))

        // Return the previous state.
        return@runCatching previousState
    }

    public override suspend fun delete(path: FolderPath): Result<FolderState> = runCatching {
        // Check that a folder exists at the given path.
        val previousState = this.state[path] ?: run {
            throw FolderNotFoundException(path)
        }
        val folder = Folder(this, path)

        // Post event.
        eventScope.post(FolderDeleteEvent(folder))

        // Update storage & state.
        val handle = handleOrThrow()
        if (StorageCapability.RECURSIVE_DELETE !in storage.capabilities) {
            // We need to manually and recursivly delete the content of the folder before moving the folder itself.
            // TODO: This should probably be handle more transaction-like
            val childWarps = state.warps.keys
            val childFolders = state.folders.keys
                .filter { it.isSubpathOf(path) }
                .sortedByDescending { it.value.length }
                .toSet()

            for (warp in childWarps) {
                storage.deleteWarp(handle, warp).getOrThrow()
            }
            for (folder in childFolders) {
                storage.deleteFolder(handle, folder).getOrThrow()
            }
            state.warps.keys.removeAll(childWarps)
            state.folders.keys.removeAll(childFolders)
        }
        storage.deleteFolder(handle, path).getOrThrow()
        this.state.delete(path)

        // Post event.
        eventScope.post(FolderPostDeleteEvent(path, previousState))

        // Return the previous state.
        return@runCatching previousState
    }

    public override suspend fun move(
        src: WarpPath,
        dst: WarpPath,
        duplicatesStrategy: DuplicatesStrategy,
    ): Result<Unit> = runCatching {
        // Handle case if a warp already exists at the destination path.
        if (dst in this) {
            when (duplicatesStrategy) {
                DuplicatesStrategy.REPLACE_EXISTING -> {
                    // TODO: This should happen as a "transaction".
                    update(dst, state[src]!!).getOrThrow()
                    delete(src)
                    return@runCatching
                }
                DuplicatesStrategy.SKIP -> return@runCatching
                DuplicatesStrategy.FAIL -> throw WarpAlreadyExistsException(dst)
            }
        }

        // Update storage.
        val handle = handleOrThrow()
        storage.moveWarp(handle, src, dst)

        TODO()
    }

    public override suspend fun move(
        src: FolderPath,
        dst: FolderPath,
        duplicatesStrategy: DuplicatesStrategy,
    ): Result<Unit> {
        if (src != dst) {
            // Nothing to do.
            return Result.success(Unit)
        }
        if (dst.isSubpathOf(src)) {
            throw FolderMoveIntoChildException(src, dst)
        }

        // Fail if a folder already exists at the destination path.
        if (dst in this) {
            when (duplicatesStrategy) {
                DuplicatesStrategy.REPLACE_EXISTING -> TODO() // We need to merge the content.
                DuplicatesStrategy.SKIP -> TODO() // We need to merge the content.
                DuplicatesStrategy.FAIL -> return Result.failure(FolderAlreadyExistsException(dst))
            }
        }

        // Update storage.
        val handle = handleOrThrow()
        if (StorageCapability.RECURSIVE_MOVE !in storage.capabilities) {
            // We first need to recursivly move the content of the folder before moving the folder itself.
            TODO()
        }
        storage.moveFolder(handle, src, dst)

        TODO()
    }

    public suspend fun moveInto(
        src: FolderPath,
        dst: NodeParentPath,
        duplicatesStrategy: DuplicatesStrategy,
    ): Result<Unit> {
        return when (dst) {
            is FolderPath -> {
                move(src, dst, duplicatesStrategy)
            }
            RootPath -> {
                if (duplicatesStrategy == DuplicatesStrategy.FAIL) {
                    return Result.failure(NodeParentAlreadyExistsException(dst))
                }

                // If replace strategy is used, overwrite root node state.
                if (duplicatesStrategy == DuplicatesStrategy.REPLACE_EXISTING) {
                    val srcState = state[src] ?: throw FolderNotFoundException(src)
                    update(RootPath, srcState)
                }

                // Move all direct child folders
                val directChildFolders = state.folders.keys
                    .filter { it.isProperSubpathOf(src) && !it.value.substring(src.value.length, it.value.length - 1).contains("/") }
                for (folder in directChildFolders) {
                    move(
                        folder,
                        RootPath / (folder.relativeTo(src)!! as FolderPath),
                        duplicatesStrategy = duplicatesStrategy,
                    ).getOrElse { return Result.failure(it) }
                }

                // Move all direct child warps.
                val directChildWarps = state.warps.keys
                    .filter { it.isSubpathOf(src) && !it.value.substring(src.value.length).contains("/") }
                for (warp in directChildWarps) {
                    move(
                        warp,
                        RootPath / warp.relativeTo(src)!!,
                        duplicatesStrategy = duplicatesStrategy,
                    ).getOrElse { return Result.failure(it) }
                }

                // Everything completed successfully.
                Result.success(Unit)
            }
        }
    }
}
