package net.voxelpi.varp.tree

import net.voxelpi.event.EventScope
import net.voxelpi.event.eventScope
import net.voxelpi.varp.DuplicatesStrategy
import net.voxelpi.varp.exception.tree.FolderAlreadyExistsException
import net.voxelpi.varp.exception.tree.FolderMoveIntoChildException
import net.voxelpi.varp.exception.tree.FolderNotFoundException
import net.voxelpi.varp.exception.tree.NodeParentNotFoundException
import net.voxelpi.varp.exception.tree.WarpAlreadyExistsException
import net.voxelpi.varp.exception.tree.WarpNotFoundException
import net.voxelpi.varp.option.DuplicatesStrategyOption
import net.voxelpi.varp.option.OptionValue
import net.voxelpi.varp.option.OptionsContext
import net.voxelpi.varp.repository.Repository
import net.voxelpi.varp.tree.path.FolderPath
import net.voxelpi.varp.tree.path.NodeChildPath
import net.voxelpi.varp.tree.path.NodeParentPath
import net.voxelpi.varp.tree.path.NodePath
import net.voxelpi.varp.tree.path.RootPath
import net.voxelpi.varp.tree.path.WarpPath
import net.voxelpi.varp.tree.state.FolderState
import net.voxelpi.varp.tree.state.WarpState

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
        return repository.registryView.warps.keys.map { path -> Warp(this, path) }
    }

    /**
     * Returns an [Collection] of all registered folders.
     */
    public fun folders(): Collection<Folder> {
        return repository.registryView.folders.keys.map { path -> Folder(this, path) }
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
    public suspend fun createWarp(path: WarpPath, state: WarpState): Result<Warp> {
        // Check if the warp already exists.
        if (exists(path)) {
            return Result.failure(WarpAlreadyExistsException(path))
        }

        // Check if the parent exists.
        if (!exists(path.parent)) {
            return Result.failure(NodeParentNotFoundException(path.parent))
        }

        // Create the warp state.
        repository.create(path, state).onFailure { return Result.failure(it) }

        val warp = Warp(this, path)
        return Result.success(warp)
    }

    /**
     * Creates a new folder at the given [path] with the given [state].
     */
    public suspend fun createFolder(path: FolderPath, state: FolderState): Result<Folder> {
        // Check if the folder already exists.
        if (exists(path)) {
            return Result.failure(FolderAlreadyExistsException(path))
        }

        // Check if the parent exists.
        if (!exists(path.parent)) {
            return Result.failure(NodeParentNotFoundException(path.parent))
        }

        // Save the folder state.
        repository.create(path, state).onFailure { return Result.failure(it) }

        val folder = Folder(this, path)
        return Result.success(folder)
    }

    /**
     * Deletes the [Warp] at the given [path].
     */
    public suspend fun deleteWarp(path: WarpPath): Result<Unit> {
        // Check if the warp exists.
        if (!exists(path)) {
            return Result.failure(WarpNotFoundException(path))
        }

        // Delete the warp state.
        repository.delete(path).onFailure { return Result.failure(it) }

        return Result.success(Unit)
    }

    /**
     * Deletes the [Folder] at the given [path].
     */
    public suspend fun deleteFolder(path: FolderPath): Result<Unit> {
        // Check if the folder exists.
        if (!exists(path)) {
            return Result.failure(FolderNotFoundException(path))
        }

        // Delete the folder state.
        repository.delete(path).onFailure { return Result.failure(it) }

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
        return repository.registryView.warps[path]
    }

    /**
     * Sets the state of the warp at the given [path].
     */
    public suspend fun warpState(path: WarpPath, state: WarpState): Result<Unit> {
        // Check if a warp exists at the given path.
        if (!exists(path)) {
            return Result.failure(WarpNotFoundException(path))
        }

        // Save the warp state at the given path.
        repository.save(path, state).onFailure { return Result.failure(it) }

        return Result.success(Unit)
    }

    /**
     * Returns the [FolderState] at the given [path].
     */
    public fun folderState(path: FolderPath): FolderState? {
        return repository.registryView.folders[path]
    }

    /**
     * Sets the state of the folder at the given [path].
     */
    public suspend fun folderState(path: FolderPath, state: FolderState): Result<Unit> {
        // Check if a folder exists at the given path.
        if (!exists(path)) {
            return Result.failure(FolderNotFoundException(path))
        }

        // Save the folder state at the given path.
        repository.save(path, state).onFailure { return Result.failure(it) }

        return Result.success(Unit)
    }

    /**
     * Returns the [FolderState] of the root.
     */
    public fun rootState(): FolderState {
        return repository.registryView.root
    }

    /**
     * Sets the state of the root.
     */
    public suspend fun rootState(state: FolderState): Result<Unit> {
        // Save the module state at the given path.
        repository.save(state).onFailure { return Result.failure(it) }

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
        return repository.registryView.warps.contains(path)
    }

    /**
     * Checks if a folder with the given [path] exists.
     */
    public fun exists(path: FolderPath): Boolean {
        return repository.registryView.folders.contains(path)
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
    public suspend fun move(src: WarpPath, dst: WarpPath, options: Collection<OptionValue<*>> = emptyList()): Result<Unit> {
        // Early return if source and destination path are the same.
        if (src == dst) {
            return Result.success(Unit)
        }

        // Create option context.
        val optionsContext = OptionsContext(options)

        // Fail if a warp already exists at the destination path.
        if (exists(dst)) {
            val duplicatesStrategy = optionsContext.getOrDefault(DuplicatesStrategyOption)
            when (duplicatesStrategy) {
                DuplicatesStrategy.REPLACE_EXISTING -> TODO()
                DuplicatesStrategy.SKIP -> TODO()
                DuplicatesStrategy.FAIL -> return Result.failure(WarpAlreadyExistsException(dst))
            }
        }

        // Move the state.
        repository.move(src, dst, optionsContext).onFailure { return Result.failure(it) }

        return Result.success(Unit)
    }

    /**
     * Moves the folder at [src] to [dst].
     */
    public suspend fun move(src: FolderPath, dst: FolderPath, options: Collection<OptionValue<*>> = emptyList()): Result<Unit> {
        // Early return if source and destination path are the same.
        if (src == dst) {
            return Result.success(Unit)
        }

        // Throw exception when trying to move a folder into one of its children.
        if (src.isTrueSubPathOf(dst)) {
            return Result.failure(FolderMoveIntoChildException(src, dst))
        }

        // Create option context.
        val optionsContext = OptionsContext(options)

        // Fail if a folder already exists at the destination path.
        if (exists(dst)) {
            val duplicatesStrategy = optionsContext.getOrDefault(DuplicatesStrategyOption)
            when (duplicatesStrategy) {
                DuplicatesStrategy.REPLACE_EXISTING -> TODO()
                DuplicatesStrategy.SKIP -> TODO()
                DuplicatesStrategy.FAIL -> return Result.failure(FolderAlreadyExistsException(dst))
            }
        }

        // Move the state.
        repository.move(src, dst, optionsContext).onFailure { return Result.failure(it) }

        return Result.success(Unit)
    }
}
