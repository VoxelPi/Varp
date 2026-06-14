package net.voxelpi.varp.tree.state

import net.voxelpi.varp.tree.path.FolderPath
import net.voxelpi.varp.tree.path.NodeParentPath
import net.voxelpi.varp.tree.path.RootPath
import net.voxelpi.varp.tree.path.WarpPath

public data class MutableTreeState(
    override val warps: MutableMap<WarpPath, WarpState> = mutableMapOf(),
    override val folders: MutableMap<FolderPath, FolderState> = mutableMapOf(),
    override var root: FolderState = FolderState.defaultRootState(),
) : TreeState {

    public operator fun set(path: WarpPath, state: WarpState) {
        warps[path] = state
    }

    public operator fun set(path: FolderPath, state: FolderState) {
        folders[path] = state
    }

    public operator fun set(path: NodeParentPath, state: FolderState) {
        when (path) {
            is FolderPath -> folders[path] = state
            RootPath -> root = state
        }
    }

    public operator fun set(path: NodeParentPath, state: TreeState) {
        when (path) {
            RootPath -> update(state)
            is FolderPath -> {
                delete(path)
                this[path] = state
                folders.putAll(state.folders.mapKeys { path / it.key })
                warps.putAll(state.warps.mapKeys { path / it.key })
            }
        }
    }

    public fun move(src: WarpPath, dst: WarpPath): WarpState? {
        val state = warps[src] ?: return null
        warps[dst] = state
        warps.remove(src)
        return state
    }

    public fun move(src: FolderPath, dst: FolderPath): FolderState? {
        // Move child warps.
        val childWarps = warps.filter { it.key.isProperSubpathOf(src) }
        warps.keys.removeAll(childWarps.keys)
        warps.putAll(childWarps.map { (path, state) -> WarpPath("${dst}${path.relativeTo(src)!!.toString().substring(1)}") to state }.toMap())

        // Move child folders.
        val childFolders = folders.filter { it.key.isProperSubpathOf(src) }
        folders.keys.removeAll(childFolders.keys)
        folders.putAll(childFolders.map { (path, state) -> FolderPath("${dst}${path.relativeTo(src)!!.toString().substring(1)}") to state }.toMap())

        // Move folder.
        val state = folders[src] ?: return null
        folders[dst] = state
        folders.remove(src)
        return state
    }

    public fun delete(path: WarpPath): WarpState? {
        return warps.remove(path)
    }

    public fun delete(path: FolderPath): FolderState? {
        // Delete child warps.
        warps.keys.removeAll { it.isProperSubpathOf(path) }

        // Delete child folders.
        folders.keys.removeAll { it.isProperSubpathOf(path) }

        // Delete folder.
        return folders.remove(path)
    }

    public fun clear() {
        warps.clear()
        folders.clear()
        root = FolderState.defaultRootState()
    }

    public fun update(newState: TreeState) {
        warps.clear()
        folders.clear()
        root = newState.root
        folders.putAll(newState.folders)
        warps.putAll(newState.warps)
    }
}
