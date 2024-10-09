package net.voxelpi.varp.api.warp.node

import net.voxelpi.varp.api.warp.Folder
import net.voxelpi.varp.api.warp.Warp
import net.voxelpi.varp.api.warp.path.NodeParentPath
import net.voxelpi.varp.api.warp.state.FolderState
import net.voxelpi.varp.api.warp.state.WarpState

/**
 * A node that can contain child nodes.
 */
interface NodeParent : Node {

    /**
     * The path to this container.
     */
    override val path: NodeParentPath

    /**
     * List of all warps that are direct children of this node.
     */
    fun childWarps(): Iterable<Warp>

    /**
     * List of all folders that are direct children of this node.
     */
    fun childFolders(): Iterable<Folder>

    /**
     * Returns if the container has a direct child warp with the given name.
     *
     * @param id the name of the warp.
     *
     * @return true if such a warp exists, otherwise false.
     */
    fun hasChildWarp(id: String): Boolean

    /**
     * Returns if the container has a direct child folder with the given name.
     *
     * @param id the name of the folder.
     *
     * @return true if such a folder exists, otherwise false.
     */
    fun hasChildFolder(id: String): Boolean

    /**
     * Returns the child warp with the given name.
     *
     * @param id the name of the warp.
     *
     * @return the warp with the given name.
     */
    fun childWarp(id: String): Warp?

    /**
     * Returns the child folder with the given name.
     *
     * @param id the name of the folder.
     *
     * @return the folder with the given name.
     */
    fun childFolder(id: String): Folder?

    /**
     * Creates a warp in this container with the given [id] and [state].
     */
    fun createWarp(id: String, state: WarpState): Result<Warp>

    /**
     * Creates a folder in this container with the given [id] and [state].
     */
    fun createFolder(id: String, state: FolderState): Result<Folder>
}
