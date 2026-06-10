package net.voxelpi.varp.repository

import net.voxelpi.event.EventScope
import net.voxelpi.event.EventScopeProvider
import net.voxelpi.event.eventScope
import net.voxelpi.event.post
import net.voxelpi.varp.tree.path.FolderPath
import net.voxelpi.varp.tree.path.WarpPath
import net.voxelpi.varp.tree.state.FolderState
import net.voxelpi.varp.tree.state.TreeState
import net.voxelpi.varp.tree.state.WarpState

/**
 * Convenience wrapper around a [Storage] and a fixed configuration instance.
 *
 * A storage instance owns at most one currently open [StorageHandle]. It forwards all operations to
 * [storage] using the configured [config] and the current handle, so callers do not need to pass the
 * configuration and handle manually for every operation.
 *
 * Callers are responsible for calling [open] before invoking any tree operation and for calling
 * [close] when the storage is no longer needed. All methods except [open] require the instance to be
 * open and will fail if no handle is available.
 *
 * This class does not perform logical tree validation. Callers must still uphold the same guarantees
 * expected by a [Storage], such as only creating nodes whose parent exists, only updating existing
 * nodes, and only moving nodes to free destination paths.
 *
 * @param C the storage configuration type.
 * @param H the storage handle type.
 * @property storage the underlying storage implementation.
 * @property config the configuration used for all operations on this instance.
 */
public data class StorageInstance<C : Any, H : StorageHandle>(
    public val storage: Storage<C, H>,
    public val config: C,
) : EventScopeProvider {
    /**
     * The currently open storage handle, or `null` if this instance is closed.
     */
    public var handle: H? = null
        private set

    /**
     * The event scope of this instance, forwards events from the storage handle.
     */
    public override val eventScope: EventScope = eventScope()

    /**
     * Returns the currently open handle.
     *
     * @throws IllegalStateException if this storage instance is not currently open.
     */
    public fun handleOrThrow(): H {
        return handle ?: throw IllegalStateException("Storage is not open")
    }

    /**
     * Whether this storage instance currently has an open handle.
     */
    public val isOpen: Boolean
        get() = handle != null

    /**
     * Opens the underlying storage using this instance's [config].
     *
     * On success, the returned handle is stored in [handle] and reused by all subsequent operations.
     *
     * Callers must not call this method while the instance is already open.
     *
     * @return the newly opened storage handle.
     */
    public suspend fun open(): Result<H> = runCatching {
        check(!isOpen) { "Storage is already open" }
        val handle = storage.open(config).getOrThrow()
        handle.eventScope.register(eventScope)
        this.handle = handle
        return@runCatching handle
    }

    /**
     * Closes the currently open storage handle.
     *
     * On invocation, the handle is removed from this instance before delegating to [Storage.close].
     * This means the instance is considered closed even if the underlying close operation returns a
     * failure.
     *
     * Callers must only call this method while the instance is open.
     */
    public suspend fun close(): Result<Unit> {
        val handle = handleOrThrow()
        this.handle = null
        handle.eventScope.unregister(eventScope)
        return storage.close(config, handle)
    }

    /**
     * Loads the complete tree state currently stored in the underlying storage.
     *
     * Callers must only call this method while the instance is open.
     *
     * @return the full persisted tree state, including the root folder, folders, and warps.
     */
    public suspend fun loadTree(): Result<TreeState> = runCatching {
        val handle = handleOrThrow()
        val state = storage.loadTree(config, handle).getOrThrow()
        eventScope.post(StorageEvents.TreeStateChangeEvent(state))
        return@runCatching state
    }

    /**
     * Replaces the complete persisted tree state with [state].
     *
     * Callers must only call this method while the instance is open.
     *
     * The underlying storage is expected to apply the replacement safely: if the operation fails, the
     * storage should not be left in a partially replaced state.
     */
    public suspend fun updateTree(state: TreeState): Result<Unit> = runCatching {
        val handle = handleOrThrow()
        storage.updateTree(config, handle, state).getOrThrow()
        eventScope.post(StorageEvents.TreeStateChangeEvent(state))
    }

    /**
     * Creates a new warp at [path] with the given [state].
     *
     * Callers must guarantee that this instance is open, that the parent folder exists, and that no
     * other warp already exists at [path].
     */
    public suspend fun createWarp(path: WarpPath, state: WarpState): Result<Unit> = runCatching {
        val handle = handleOrThrow()
        storage.createWarp(config, handle, path, state).getOrThrow()
        eventScope.post(StorageEvents.WarpCreateEvent(path, state))
    }

    /**
     * Creates a new folder at [path] with the given [state].
     *
     * Callers must guarantee that this instance is open, that the parent folder exists, and that no
     * other folder already exists at [path].
     */
    public suspend fun createFolder(path: FolderPath, state: FolderState): Result<Unit> = runCatching {
        val handle = handleOrThrow()
        storage.createFolder(config, handle, path, state).getOrThrow()
        eventScope.post(StorageEvents.FolderCreateEvent(path, state))
    }

    /**
     * Updates the state of an existing warp at [path].
     *
     * Callers must guarantee that this instance is open and that a warp exists at [path].
     */
    public suspend fun updateWarp(path: WarpPath, state: WarpState): Result<Unit> = runCatching {
        val handle = handleOrThrow()
        storage.updateWarp(config, handle, path, state).getOrThrow()
        eventScope.post(StorageEvents.WarpStateChangeEvent(path, state))
    }

    /**
     * Updates the state of an existing folder at [path].
     *
     * Callers must guarantee that this instance is open and that a folder exists at [path].
     */
    public suspend fun updateFolder(path: FolderPath, state: FolderState): Result<Unit> = runCatching {
        val handle = handleOrThrow()
        storage.updateFolder(config, handle, path, state).getOrThrow()
        eventScope.post(StorageEvents.FolderStateChangeEvent(path, state))
    }

    /**
     * Updates the state of the root folder.
     *
     * Callers must guarantee that this instance is open.
     */
    public suspend fun updateRoot(state: FolderState): Result<Unit> = runCatching {
        val handle = handleOrThrow()
        storage.updateRoot(config, handle, state).getOrThrow()
        eventScope.post(StorageEvents.RootStateChangeEvent(state))
    }

    /**
     * Deletes the warp at [path].
     *
     * Callers must guarantee that this instance is open and that a warp exists at [path].
     *
     * If the operation fails, the underlying storage is expected to keep the warp available.
     */
    public suspend fun deleteWarp(path: WarpPath): Result<Unit> = runCatching {
        val handle = handleOrThrow()
        storage.deleteWarp(config, handle, path).getOrThrow()
        eventScope.post(StorageEvents.WarpDeleteEvent(path))
    }

    /**
     * Deletes the folder at [path] and all descendant folders and warps.
     *
     * Callers must guarantee that this instance is open and that a folder exists at [path].
     *
     * If the operation fails, the underlying storage is expected to keep the full subtree available
     * instead of leaving only part of it deleted.
     */
    public suspend fun deleteFolder(path: FolderPath): Result<Unit> = runCatching {
        val handle = handleOrThrow()
        storage.deleteFolder(config, handle, path).getOrThrow()
        eventScope.post(StorageEvents.FolderDeleteEvent(path))
    }

    /**
     * Moves a warp from [src] to [dst].
     *
     * Callers must guarantee that this instance is open, that a warp exists at [src], that the
     * destination parent folder exists, and that no node already exists at [dst].
     */
    public suspend fun moveWarp(src: WarpPath, dst: WarpPath): Result<Unit> = runCatching {
        val handle = handleOrThrow()
        storage.moveWarp(config, handle, src, dst).getOrThrow()
        eventScope.post(StorageEvents.WarpPathChangeEvent(src, dst))
    }

    /**
     * Moves a folder from [src] to [dst], including all descendant folders and warps.
     *
     * Callers must guarantee that this instance is open, that a folder exists at [src], that the
     * destination parent folder exists, and that no node already exists at [dst].
     *
     * The full subtree state is preserved. If the operation fails, the underlying storage is expected
     * to keep the subtree at [src] instead of leaving it partially moved.
     */
    public suspend fun moveFolder(src: FolderPath, dst: FolderPath): Result<Unit> = runCatching {
        val handle = handleOrThrow()
        storage.moveFolder(config, handle, src, dst).getOrThrow()
        eventScope.post(StorageEvents.FolderPathChangeEvent(src, dst))
    }
}
