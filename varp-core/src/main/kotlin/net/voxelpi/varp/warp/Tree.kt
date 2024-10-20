package net.voxelpi.varp.warp

import net.voxelpi.event.EventScope
import net.voxelpi.event.eventScope
import net.voxelpi.event.post
import net.voxelpi.varp.DuplicatesStrategy
import net.voxelpi.varp.event.folder.FolderCreateEvent
import net.voxelpi.varp.event.folder.FolderDeleteEvent
import net.voxelpi.varp.event.folder.FolderPathChangeEvent
import net.voxelpi.varp.event.folder.FolderPostDeleteEvent
import net.voxelpi.varp.event.folder.FolderStateChangeEvent
import net.voxelpi.varp.event.root.RootStateChangeEvent
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
import net.voxelpi.varp.warp.path.FolderPath
import net.voxelpi.varp.warp.path.NodeChildPath
import net.voxelpi.varp.warp.path.NodeParentPath
import net.voxelpi.varp.warp.path.NodePath
import net.voxelpi.varp.warp.path.RootPath
import net.voxelpi.varp.warp.path.WarpPath
import net.voxelpi.varp.warp.repository.Repository
import net.voxelpi.varp.warp.state.FolderState
import net.voxelpi.varp.warp.state.WarpState

/**
 * Manages all registered warps.
 *
 * @property repository the repository of this tree.
 */
public class Tree internal constructor(
    public val repository: Repository,
) {

    public val eventScope: EventScope = eventScope()

    /**
     * The root of the node tree (the "/" folder)
     */
    public val root: Root = Root(this)

    /**
     * Returns an [Collection] of all registered warps.
     */
    public fun warps(): Collection<Warp> {
        return repository.registry.warps.keys.map { path -> Warp(this, path) }
    }

    /**
     * Returns an [Collection] of all registered folders.
     */
    public fun folders(): Collection<Folder> {
        return repository.registry.folders.keys.map { path -> Folder(this, path) }
    }

    /**
     * All containers of the collection.
     */
    public fun containers(): Iterable<NodeParent> {
        return folders() + listOf(root)
    }

    /**
     * Creates a new warp at the given [path] with the given [state].
     */
    public fun createWarp(path: WarpPath, state: WarpState): Result<Warp> {
        // Check if the warp already exists.
        if (exists(path)) {
            return Result.failure(WarpAlreadyExistsException(path))
        }

        // Check if the parent exists.
        if (!exists(path.parent)) {
            return Result.failure(NodeParentNotFoundException(path.parent))
        }

        // Create the warp state.
        repository.createWarpState(path, state).onFailure { return Result.failure(it) }
        val warp = Warp(this, path)

        // Post event.
        eventScope.post(WarpCreateEvent(warp))

        return Result.success(warp)
    }

    /**
     * Creates a new folder at the given [path] with the given [state].
     */
    public fun createFolder(path: FolderPath, state: FolderState): Result<Folder> {
        // Check if the folder already exists.
        if (exists(path)) {
            return Result.failure(FolderAlreadyExistsException(path))
        }

        // Check if the parent exists.
        if (!exists(path.parent)) {
            return Result.failure(NodeParentNotFoundException(path.parent))
        }

        // Save the folder state.
        repository.createFolderState(path, state).onFailure { return Result.failure(it) }
        val folder = Folder(this, path)

        // Post event.
        eventScope.post(FolderCreateEvent(folder))

        return Result.success(folder)
    }

    /**
     * Deletes the [Warp] at the given [path].
     */
    public fun deleteWarp(path: WarpPath): Result<Unit> {
        // Check if the warp exists.
        val state = warpState(path) ?: return Result.failure(WarpNotFoundException(path))

        // Post event.
        eventScope.post(WarpDeleteEvent(Warp(this, path)))

        // Delete the warp state.
        repository.deleteWarpState(path)

        // Post event.
        eventScope.post(WarpPostDeleteEvent(path, state))
        return Result.success(Unit)
    }

    /**
     * Deletes the [Folder] at the given [path].
     */
    public fun deleteFolder(path: FolderPath): Result<Unit> {
        // Check if the folder exists.
        val state = folderState(path) ?: return Result.failure(FolderNotFoundException(path))

        // Post event.
        eventScope.post(FolderDeleteEvent(Folder(this, path)))

        // Delete the folder state.
        repository.deleteFolderState(path)

        // Post event.
        eventScope.post(FolderPostDeleteEvent(path, state))
        return Result.success(Unit)
    }

    /**
     * Returns the [Warp] at the given [path].
     */
    public fun resolve(path: WarpPath): Warp? {
        if (!exists(path)) {
            return null
        }
        return Warp(this, path)
    }

    /**
     * Returns the [Folder] at the given [path].
     */
    public fun resolve(path: FolderPath): Folder? {
        if (!exists(path)) {
            return null
        }
        return Folder(this, path)
    }

    /**
     * Returns the [Root] at the given [path].
     */
    @Suppress("unused")
    public fun resolve(path: RootPath): Root {
        return root
    }

    /**
     * Returns the [NodeParent] at the given [path].
     */
    public fun resolve(path: NodeParentPath): NodeParent? {
        return when (path) {
            is RootPath -> root
            is FolderPath -> resolve(path)
        }
    }

    /**
     * Returns the [NodeChild] at the given [path].
     */
    public fun resolve(path: NodeChildPath): NodeChild? {
        return when (path) {
            is WarpPath -> resolve(path)
            is FolderPath -> resolve(path)
        }
    }

    /**
     * Returns the [Node] at the given [path].
     */
    public fun resolve(path: NodePath): Node? {
        return when (path) {
            is WarpPath -> resolve(path)
            is FolderPath -> resolve(path)
            RootPath -> root
        }
    }

    /**
     * Returns the [WarpState] at the given [path].
     */
    public fun warpState(path: WarpPath): WarpState? {
        return repository.registry.warps[path]
    }

    /**
     * Sets the state of the warp at the given [path].
     */
    public fun warpState(path: WarpPath, state: WarpState): Result<Unit> {
        // Check if a warp exists at the given path.
        val previousState = warpState(path) ?: run {
            return Result.failure(WarpNotFoundException(path))
        }

        // Save the warp state at the given path.
        repository.saveWarpState(path, state).onFailure { return Result.failure(it) }

        // Post event.
        eventScope.post(WarpStateChangeEvent(Warp(this, path), state, previousState))

        return Result.success(Unit)
    }

    /**
     * Returns the [FolderState] at the given [path].
     */
    public fun folderState(path: FolderPath): FolderState? {
        return repository.registry.folders[path]
    }

    /**
     * Sets the state of the folder at the given [path].
     */
    public fun folderState(path: FolderPath, state: FolderState): Result<Unit> {
        // Check if a folder exists at the given path.
        val previousState = folderState(path) ?: run {
            return Result.failure(FolderNotFoundException(path))
        }

        // Save the folder state at the given path.
        repository.saveFolderState(path, state).onFailure { return Result.failure(it) }

        // Post event.
        eventScope.post(FolderStateChangeEvent(Folder(this, path), state, previousState))

        return Result.success(Unit)
    }

    /**
     * Returns the [FolderState] of the root.
     */
    public fun rootState(): FolderState {
        return repository.registry.root
    }

    /**
     * Sets the state of the root.
     */
    public fun rootState(state: FolderState): Result<Unit> {
        // Check if a module exists at the given path.
        val previousState = rootState()

        // Save the module state at the given path.
        repository.saveRootState(state).onFailure { return Result.failure(it) }

        // Post event.
        eventScope.post(RootStateChangeEvent(root, state, previousState))

        return Result.success(Unit)
    }

    /**
     * All warps of the collection in the given [path].
     * If [recursive] is true, warps of child directories will also be returned.
     */
    public fun warps(path: NodeParentPath, recursive: Boolean): Iterable<Warp> {
        return if (recursive) {
            warps().filter { path.isTrueSubPathOf(it.path) }
        } else {
            warps().filter { it.path.parent == path }
        }
    }

    /**
     * All folders of the collection in the given [path].
     * If [recursive] is true, folders of child directories will also be returned.
     */
    public fun folders(path: NodeParentPath, recursive: Boolean): Iterable<Folder> {
        return if (recursive) {
            folders().filter { path.isTrueSubPathOf(it.path) }
        } else {
            folders().filter { it.path.parent == path }
        }
    }

    /**
     * Returns an [Iterable] containing all warps of the collection in the given [path] that match the given [predicate].
     * If [recursive] is true, warps of child directories will also be returned.
     */
    public fun warps(path: NodeParentPath, recursive: Boolean, predicate: (Warp) -> Boolean): Collection<Warp> {
        return warps(path, recursive).filter(predicate)
    }

    /**
     * Returns an [Iterable] containing all folders of the collection in the given [path] that match the given [predicate].
     * If [recursive] is true, folders of child directories will also be returned.
     */
    public fun folders(path: NodeParentPath, recursive: Boolean, predicate: (Folder) -> Boolean): Collection<Folder> {
        return folders(path, recursive).filter(predicate)
    }

    /**
     * Returns an [Iterable] containing all warps matching the given [predicate].
     */
    public fun warps(predicate: (Warp) -> Boolean): Iterable<Warp> {
        return warps().filter(predicate)
    }

    /**
     * Returns an [Iterable] containing all folders matching the given [predicate].
     */
    public fun folders(predicate: (Folder) -> Boolean): Iterable<Folder> {
        return folders().filter(predicate)
    }

    /**
     * Returns an [Iterable] containing all containers matching the given [predicate].
     */
    public fun containers(predicate: (NodeParent) -> Boolean): Iterable<NodeParent> {
        return containers().filter(predicate)
    }

    /**
     * Checks if a warp with the given [path] exists.
     */
    public fun exists(path: WarpPath): Boolean {
        return repository.registry.warps.contains(path)
    }

    /**
     * Checks if a folder with the given [path] exists.
     */
    public fun exists(path: FolderPath): Boolean {
        return repository.registry.folders.contains(path)
    }

    /**
     * Checks if the root exists. This is always true and this method exists just for completeness.
     */
    @Suppress("unused")
    public fun exists(path: RootPath): Boolean {
        return true
    }

    /**
     * Checks if a node parent with the given [path] exists.
     */
    public fun exists(path: NodeParentPath): Boolean {
        return when (path) {
            is RootPath -> true
            is FolderPath -> exists(path)
        }
    }

    /**
     * Checks if a node parent with the given [path] exists.
     */
    public fun exists(path: NodeChildPath): Boolean {
        return when (path) {
            is FolderPath -> exists(path)
            is WarpPath -> exists(path)
        }
    }

    /**
     * Checks if a node parent with the given [path] exists.
     */
    public fun exists(path: NodePath): Boolean {
        return when (path) {
            is RootPath -> true
            is FolderPath -> exists(path)
            is WarpPath -> exists(path)
        }
    }

    /**
     * Moves the warp at [src] to [dst].
     */
    public fun move(src: WarpPath, dst: WarpPath, duplicatesStrategy: DuplicatesStrategy): Result<Unit> {
        // Early return if source and destination path are the same.
        if (src == dst) {
            return Result.success(Unit)
        }

        // Fail if a warp already exists at the destination path.
        if (exists(dst)) {
            when (duplicatesStrategy) {
                DuplicatesStrategy.REPLACE_EXISTING -> TODO()
                DuplicatesStrategy.SKIP -> TODO()
                DuplicatesStrategy.FAIL -> return Result.failure(WarpAlreadyExistsException(dst))
            }
        }

        // Move the state.
        repository.moveWarpState(src, dst).onFailure { return Result.failure(it) }

        // Post event.
        eventScope.post(WarpPathChangeEvent(Warp(this, dst), dst, src))

        return Result.success(Unit)
    }

    /**
     * Moves the folder at [src] to [dst].
     */
    public fun move(src: FolderPath, dst: FolderPath, duplicatesStrategy: DuplicatesStrategy): Result<Unit> {
        // Early return if source and destination path are the same.
        if (src == dst) {
            return Result.success(Unit)
        }

        // Throw exception when trying to move a folder into one of its children.
        if (src.isTrueSubPathOf(dst)) {
            return Result.failure(FolderMoveIntoChildException(src, dst))
        }

        // Fail if a folder already exists at the destination path.
        if (exists(dst)) {
            when (duplicatesStrategy) {
                DuplicatesStrategy.REPLACE_EXISTING -> TODO()
                DuplicatesStrategy.SKIP -> TODO()
                DuplicatesStrategy.FAIL -> return Result.failure(FolderAlreadyExistsException(dst))
            }
        }

        // Move the state.
        repository.moveFolderState(src, dst).onFailure { return Result.failure(it) }

        // Post event.
        eventScope.post(FolderPathChangeEvent(Folder(this, dst), dst, src))

        return Result.success(Unit)
    }
}
