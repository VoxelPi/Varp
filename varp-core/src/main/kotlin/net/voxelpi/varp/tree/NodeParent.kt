package net.voxelpi.varp.tree

import net.voxelpi.varp.tree.path.NodeParentPath
import net.voxelpi.varp.tree.state.FolderState
import net.voxelpi.varp.tree.state.WarpState

/**
 * A node that can contain child nodes.
 */
public sealed interface NodeParent : Node {

    /**
     * The path to this container.
     */
    override val path: NodeParentPath

    override val state: FolderState

    /**
     * Modifies the state of the folder.
     */
    public suspend fun modify(state: FolderState): Result<Unit>

    /**
     * Modifies the state of the folder.
     */
    public suspend fun modify(init: FolderState.Builder.() -> Unit): Result<FolderState> = runCatching {
        val builder = FolderState.Builder(state)
        builder.init()
        val state = builder.build()
        modify(state).getOrThrow()
        state
    }

    /**
     * List of all warps that are direct children of this node.
     */
    public fun childWarps(): Iterable<Warp> {
        return tree.warps(path, false)
    }

    /**
     * List of all folders that are direct children of this node.
     */
    public fun childFolders(): Iterable<Folder> {
        return tree.folders(path, false)
    }

    /**
     * Returns if the container has a direct child warp with the given name.
     *
     * @param id the name of the warp.
     *
     * @return true if such a warp exists, otherwise false.
     */
    public fun hasChildWarp(id: String): Boolean {
        return childWarps().any { it.id == id }
    }

    /**
     * Returns if the container has a direct child folder with the given name.
     *
     * @param id the name of the folder.
     *
     * @return true if such a folder exists, otherwise false.
     */
    public fun hasChildFolder(id: String): Boolean {
        return childFolders().any { it.id == id }
    }

    /**
     * Returns the child warp with the given name.
     *
     * @param id the name of the warp.
     *
     * @return the warp with the given name.
     */
    public fun childWarp(id: String): Warp? {
        return tree[path.warp(id)]
    }

    /**
     * Returns the child folder with the given name.
     *
     * @param id the name of the folder.
     *
     * @return the folder with the given name.
     */
    public fun childFolder(id: String): Folder? {
        return tree[path.folder(id)]
    }

    /**
     * Creates a warp in this container with the given [id] and [state].
     */
    public suspend fun createWarp(id: String, state: WarpState): Result<Warp> {
        val path = this.path.warp(id)
        return tree.create(path, state)
    }

    /**
     * Creates a folder in this container with the given [id] and [state].
     */
    public suspend fun createFolder(id: String, state: FolderState): Result<Folder> {
        val path = this.path.folder(id)
        return tree.create(path, state)
    }
}
