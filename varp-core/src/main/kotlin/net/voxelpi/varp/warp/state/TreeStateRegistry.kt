package net.voxelpi.varp.warp.state

import net.voxelpi.varp.warp.path.FolderPath
import net.voxelpi.varp.warp.path.NodeParentPath
import net.voxelpi.varp.warp.path.RootPath
import net.voxelpi.varp.warp.path.WarpPath

public data class TreeStateRegistry(
    override val warps: MutableMap<WarpPath, WarpState> = mutableMapOf(),
    override val folders: MutableMap<FolderPath, FolderState> = mutableMapOf(),
    override var root: FolderState = FolderState.defaultRootState(),
) : TreeStateRegistryView {

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

    public fun move(src: WarpPath, dst: WarpPath): WarpState? {
        val state = warps[src] ?: return null
        warps[dst] = state
        warps.remove(src)
        return state
    }

    public fun move(src: FolderPath, dst: FolderPath): FolderState? {
        // TODO: Recursive move required.
        val state = folders[src] ?: return null
        folders[dst] = state
        folders.remove(src)
        return state
    }

    public fun delete(path: WarpPath): WarpState? {
        return warps.remove(path)
    }

    public fun delete(path: FolderPath): FolderState? {
        return folders.remove(path)
    }

    public fun clear() {
        warps.clear()
        folders.clear()
        root = FolderState.defaultRootState()
    }
}
