package net.voxelpi.varp.repository

import net.voxelpi.varp.tree.path.FolderPath
import net.voxelpi.varp.tree.path.WarpPath
import net.voxelpi.varp.tree.state.FolderState
import net.voxelpi.varp.tree.state.TreeState
import net.voxelpi.varp.tree.state.WarpState
import kotlin.reflect.KClass

/**
 * Persistent backend for a Varp repository.
 *
 * A storage implementation is responsible for loading and mutating the authoritative tree state of
 * a repository. The repository layer is the only expected caller of this interface and performs all
 * logical tree validation before invoking storage operations.
 *
 * In particular, unless stated otherwise, implementations may assume that:
 *
 * - parent folders already exist when creating a node;
 * - target nodes already exist when updating, deleting, or moving a node;
 * - destination paths do not already exist when creating or moving a node;
 * - paths passed to a method have the correct type for that method.
 *
 * Implementations should nevertheless handle backend failures safely. If an operation fails, the
 * storage must not be left in a logically inconsistent or partially updated state. Operations that
 * affect multiple nodes, such as [updateTree], [deleteFolder], and [moveFolder], should therefore be
 * implemented transactionally or with an equivalent rollback strategy.
 *
 * Implementations should return failures as [Result.failure] instead of throwing exceptions directly
 * from the public API.
 *
 * @param C the storage configuration type.
 * @param H the storage handle type created by [open] and passed to subsequent operations.
 */
public interface Storage<C : Any, H : StorageHandle> {

    /**
     * The configuration type supported by this storage implementation.
     */
    public val configType: KClass<C>

    /**
     * Opens this storage for the given [config] and returns a storage handle.
     *
     * This method should initialize any resources required for later operations, such as database
     * connections, connection pools, file handles, caches, or backend clients. Implementations may
     * return a custom [StorageHandle] subtype containing those resources.
     *
     * The returned handle is passed back to all other storage methods and should remain valid until
     * [close] is called.
     */
    public suspend fun open(config: C): Result<H>

    /**
     * Closes a handle previously returned by [open].
     *
     * This method should release all resources associated with [handle], such as database
     * connections, connection pools, file handles, caches, or backend clients.
     *
     * After this method succeeds, the handle should be considered invalid and must not be used for
     * further storage operations.
     */
    public suspend fun close(config: C, handle: H): Result<Unit>

    /**
     * Loads the complete tree state currently stored in this backend.
     *
     * The returned [TreeState] must represent the full repository tree, including the root folder,
     * all descendant folders, and all warps.
     *
     * Implementations should fail if the persisted data cannot be read or cannot be converted into a
     * valid tree state.
     */
    public suspend fun loadTree(config: C, handle: H): Result<TreeState>

    /**
     * Replaces the complete persisted tree state with [state].
     *
     * This operation is a full-tree update: all currently stored nodes are removed and the nodes from
     * [state] become the new persisted repository contents.
     *
     * Implementations must not leave the backend in a partially replaced state. If the operation
     * fails, the previously stored tree should remain available as if the operation had not happened.
     */
    public suspend fun updateTree(config: C, handle: H, state: TreeState): Result<Unit>

    /**
     * Creates a new warp at [path] with the given [state].
     *
     * The repository layer guarantees that the parent folder exists and that no node already exists
     * at [path].
     *
     * If the operation fails, no partial warp data should remain visible in storage.
     */
    public suspend fun createWarp(config: C, handle: H, path: WarpPath, state: WarpState): Result<Unit>

    /**
     * Creates a new folder at [path] with the given [state].
     *
     * The repository layer guarantees that the parent folder exists and that no node already exists
     * at [path].
     *
     * If the operation fails, no partial folder data should remain visible in storage.
     */
    public suspend fun createFolder(config: C, handle: H, path: FolderPath, state: FolderState): Result<Unit>

    /**
     * Updates the state of an existing warp at [path].
     *
     * The repository layer guarantees that a warp exists at [path].
     *
     * If the operation fails, the previous warp state should remain available.
     */
    public suspend fun updateWarp(config: C, handle: H, path: WarpPath, state: WarpState): Result<Unit>

    /**
     * Updates the state of an existing folder at [path].
     *
     * The repository layer guarantees that a folder exists at [path].
     *
     * If the operation fails, the previous folder state should remain available.
     */
    public suspend fun updateFolder(config: C, handle: H, path: FolderPath, state: FolderState): Result<Unit>

    /**
     * Updates the state of the root folder.
     *
     * If the operation fails, the previous root state should remain available.
     */
    public suspend fun updateRoot(config: C, handle: H, state: FolderState): Result<Unit>

    /**
     * Deletes the warp at [path].
     *
     * The repository layer guarantees that a warp exists at [path].
     *
     * If the operation fails, the warp should remain available as if the operation had not happened.
     */
    public suspend fun deleteWarp(config: C, handle: H, path: WarpPath): Result<Unit>

    /**
     * Deletes the folder at [path] and all of its descendant folders and warps.
     *
     * The repository layer guarantees that a folder exists at [path].
     *
     * This operation must be recursive. If the operation fails, the storage must not be left with
     * only part of the subtree deleted. The folder and all descendants should remain available as if
     * the operation had not happened.
     */
    public suspend fun deleteFolder(config: C, handle: H, path: FolderPath): Result<Unit>

    /**
     * Moves a warp from [src] to [dst].
     *
     * The repository layer guarantees that a warp exists at [src], that the destination parent folder
     * exists, and that no node exists at [dst].
     *
     * Only the warp path changes. The warp state is preserved.
     *
     * If the operation fails, the warp should remain at [src] and must not also appear at [dst].
     */
    public suspend fun moveWarp(config: C, handle: H, src: WarpPath, dst: WarpPath): Result<Unit>

    /**
     * Moves a folder from [src] to [dst], including all descendant folders and warps.
     *
     * The repository layer guarantees that a folder exists at [src], that the destination parent
     * folder exists, and that no node exists at [dst].
     *
     * This operation must update the paths of all descendants consistently. If the operation fails,
     * the storage must not be left with only part of the subtree moved. The entire subtree should
     * remain at [src] as if the operation had not happened.
     */
    public suspend fun moveFolder(config: C, handle: H, src: FolderPath, dst: FolderPath): Result<Unit>

    /**
     * Creates a [StorageInstance] for this storage implementation and [config].
     */
    public fun createInstance(config: C): StorageInstance<C, H> {
        return StorageInstance(this, config)
    }
}
