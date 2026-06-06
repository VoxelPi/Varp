package net.voxelpi.varp.tree

import net.voxelpi.event.EventScope
import net.voxelpi.varp.DuplicatesStrategy
import net.voxelpi.varp.tree.path.FolderPath
import net.voxelpi.varp.tree.path.NodeChildPath
import net.voxelpi.varp.tree.path.NodeParentPath
import net.voxelpi.varp.tree.path.NodePath
import net.voxelpi.varp.tree.path.RootPath
import net.voxelpi.varp.tree.path.WarpPath
import net.voxelpi.varp.tree.state.FolderState
import net.voxelpi.varp.tree.state.TreeState
import net.voxelpi.varp.tree.state.WarpState

public interface Tree {

    public val state: TreeState

    public val eventScope: EventScope

    /**
     * The root of the node tree (the "/" folder)
     */
    public val root: Root
        get() = Root(this)

    /**
     * Returns an [Collection] of all registered warps.
     */
    public fun warps(): Collection<Warp> {
        return state.warps.keys.map { path -> Warp(this, path) }
    }

    /**
     * All warps of the collection in the given [path].
     * If [recursive] is true, warps of child directories will also be returned.
     */
    public fun warps(path: NodeParentPath, recursive: Boolean): Iterable<Warp> {
        return if (recursive) {
            warps().filter { it.path.isProperSubpathOf(path) }
        } else {
            warps().filter { it.path.parent == path }
        }
    }

    /**
     * Returns an [Collection] of all registered folders.
     */
    public fun folders(): Collection<Folder> {
        return state.folders.keys.map { path -> Folder(this, path) }
    }

    /**
     * All folders of the collection in the given [path].
     * If [recursive] is true, folders of child directories will also be returned.
     */
    public fun folders(path: NodeParentPath, recursive: Boolean): Iterable<Folder> {
        return if (recursive) {
            folders().filter { it.path.isProperSubpathOf(path) }
        } else {
            folders().filter { it.path.parent == path }
        }
    }

    /**
     * All containers of the collection.
     */
    public fun containers(): Iterable<NodeParent> {
        return folders() + listOf(root)
    }

    /**
     * Checks if a node with the given [path] exists.
     */
    public operator fun contains(path: NodePath): Boolean {
        return when (path) {
            is RootPath -> true
            is FolderPath -> path in state.folders
            is WarpPath -> path in state.warps
        }
    }

    /**
     * Returns the [Warp] at the given [path].
     */
    public operator fun get(path: WarpPath): Warp? {
        if (path !in this) {
            return null
        }
        return Warp(this, path)
    }

    /**
     * Returns the [Folder] at the given [path].
     */
    public operator fun get(path: FolderPath): Folder? {
        if (path !in this) {
            return null
        }
        return Folder(this, path)
    }

    /**
     * Returns the [Root] at the given [path].
     */
    public operator fun get(path: RootPath): Root {
        return root
    }

    /**
     * Returns the [NodeParent] at the given [path].
     */
    public operator fun get(path: NodeParentPath): NodeParent? {
        return when (path) {
            is RootPath -> root
            is FolderPath -> this[path]
        }
    }

    /**
     * Returns the [NodeChild] at the given [path].
     */
    public operator fun get(path: NodeChildPath): NodeChild? {
        return when (path) {
            is WarpPath -> this[path]
            is FolderPath -> this[path]
        }
    }

    /**
     * Returns the [Node] at the given [path].
     */
    public operator fun get(path: NodePath): Node? {
        return when (path) {
            is WarpPath -> this[path]
            is FolderPath -> this[path]
            RootPath -> root
        }
    }

    /**
     * Creates a new warp at the given [path] with the given [state].
     */
    public suspend fun create(path: WarpPath, state: WarpState): Result<Warp>

    /**
     * Creates a new folder at the given [path] with the given [state].
     */
    public suspend fun create(path: FolderPath, state: FolderState): Result<Folder>

    /**
     * Deletes the [Warp] at the given [path].
     */
    public suspend fun delete(path: WarpPath): Result<WarpState>

    /**
     * Deletes the [Folder] at the given [path].
     */
    public suspend fun delete(path: FolderPath): Result<FolderState>

    /**
     * Sets the state of the warp at the given [path].
     */
    public suspend fun update(path: WarpPath, newState: WarpState): Result<WarpState>

    /**
     * Sets the state of the folder at the given [path].
     */
    public suspend fun update(path: FolderPath, newState: FolderState): Result<FolderState>

    /**
     * Sets the state of the root.
     */
    public suspend fun update(path: RootPath, newState: FolderState): Result<FolderState>

    /**
     * Sets the state of the folder at the given [path].
     */
    public suspend fun update(path: NodeParentPath, newState: FolderState): Result<FolderState> {
        return when (path) {
            is FolderPath -> update(path, newState)
            RootPath -> update(RootPath, newState)
        }
    }

    /**
     * Moves the warp at [src] to [dst].
     */
    public suspend fun move(
        src: WarpPath,
        dst: WarpPath,
        duplicatesStrategy: DuplicatesStrategy = DuplicatesStrategy.FAIL,
    ): Result<Unit>

    /**
     * Moves the folder at [src] to [dst].
     */
    public suspend fun move(
        src: FolderPath,
        dst: FolderPath,
        duplicatesStrategy: DuplicatesStrategy = DuplicatesStrategy.FAIL,
    ): Result<Unit>
}
