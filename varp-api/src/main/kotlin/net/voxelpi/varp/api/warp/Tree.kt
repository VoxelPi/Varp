package net.voxelpi.varp.api.warp

import net.voxelpi.varp.api.warp.path.FolderPath
import net.voxelpi.varp.api.warp.path.NodeParentPath
import net.voxelpi.varp.api.warp.path.WarpPath
import net.voxelpi.varp.api.warp.state.FolderState
import net.voxelpi.varp.api.warp.state.WarpState
import net.voxelpi.varp.api.warp.tree.NodeParent

/**
 * Manages all registered warps.
 */
interface Tree {

    /**
     * The root of the node tree (the "/" folder)
     */
    val root: Root

    /**
     * Returns an [Collection] of all registered warps.
     */
    fun warps(): Collection<Warp>

    /**
     * Returns an [Collection] of all registered folders.
     */
    fun folders(): Collection<Folder>

    /**
     * All containers of the collection.
     */
    fun containers(): Iterable<NodeParent>

    /**
     * Creates a new warp at the given [path] with the given [state].
     */
    fun createWarp(path: WarpPath, state: WarpState): Result<Warp>

    /**
     * Creates a new folder at the given [path] with the given [state].
     */
    fun createFolder(path: FolderPath, state: FolderState): Result<Folder>

    /**
     * Deletes the [Warp] at the given [path].
     */
    fun deleteWarp(path: WarpPath): Result<Unit>

    /**
     * Deletes the [Folder] at the given [path].
     */
    fun deleteFolder(path: FolderPath): Result<Unit>

    /**
     * Returns the [Warp] at the given [path].
     */
    fun warp(path: WarpPath): Warp?

    /**
     * Returns the [Folder] at the given [path].
     */
    fun folder(path: FolderPath): Folder?

    /**
     * Returns the [NodeParent] at the given [path].
     */
    fun container(path: NodeParentPath): NodeParent?

    /**
     * Returns the [WarpState] at the given [path].
     */
    fun warpState(path: WarpPath): WarpState?

    /**
     * Sets the state of the warp at the given [path].
     */
    fun warpState(path: WarpPath, state: WarpState): Result<Unit>

    /**
     * Returns the [FolderState] at the given [path].
     */
    fun folderState(path: FolderPath): FolderState?

    /**
     * Sets the state of the folder at the given [path].
     */
    fun folderState(path: FolderPath, state: FolderState): Result<Unit>

    /**
     * All warps of the collection in the given [path].
     * If [recursive] is true, warps of child directories will also be returned.
     */
    fun warps(path: NodeParentPath, recursive: Boolean): Iterable<Warp>

    /**
     * All folders of the collection in the given [path].
     * If [recursive] is true, folders of child directories will also be returned.
     */
    fun folders(path: NodeParentPath, recursive: Boolean): Iterable<Folder>

    /**
     * Returns an [Iterable] containing all warps of the collection in the given [path] that match the given [predicate].
     * If [recursive] is true, warps of child directories will also be returned.
     */
    fun warps(path: NodeParentPath, recursive: Boolean, predicate: (Warp) -> Boolean): Iterable<Warp>

    /**
     * Returns an [Iterable] containing all folders of the collection in the given [path] that match the given [predicate].
     * If [recursive] is true, folders of child directories will also be returned.
     */
    fun folders(path: NodeParentPath, recursive: Boolean, predicate: (Folder) -> Boolean): Iterable<Folder>

    /**
     * Returns an [Iterable] containing all warps matching the given [predicate].
     */
    fun warps(predicate: (Warp) -> Boolean): Iterable<Warp>

    /**
     * Returns an [Iterable] containing all folders matching the given [predicate].
     */
    fun folders(predicate: (Folder) -> Boolean): Iterable<Folder>

    /**
     * Returns an [Iterable] containing all containers matching the given [predicate].
     */
    fun containers(predicate: (NodeParent) -> Boolean): Iterable<NodeParent>

    /**
     * Checks if a warp with the given [path] exists.
     */
    fun exists(path: WarpPath): Boolean

    /**
     * Checks if a folder with the given [path] exists.
     */
    fun exists(path: FolderPath): Boolean

    /**
     * Checks if a node parent with the given [path] exists.
     */
    fun exists(path: NodeParentPath): Boolean

    /**
     * Moves the warp at [src] to [dst].
     */
    fun move(src: WarpPath, dst: WarpPath): Result<Unit>

    /**
     * Moves the folder at [src] to [dst].
     */
    fun move(src: FolderPath, dst: FolderPath): Result<Unit>
}
