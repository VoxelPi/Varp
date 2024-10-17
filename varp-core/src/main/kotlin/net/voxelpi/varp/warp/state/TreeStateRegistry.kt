package net.voxelpi.varp.warp.state

import net.kyori.adventure.text.Component
import net.voxelpi.varp.warp.path.FolderPath
import net.voxelpi.varp.warp.path.WarpPath

public data class TreeStateRegistry(
    override val warps: MutableMap<WarpPath, WarpState> = mutableMapOf(),
    override val folders: MutableMap<FolderPath, FolderState> = mutableMapOf(),
    override var root: FolderState = FolderState(Component.text("root"), emptyList(), emptySet(), emptyMap()),
) : TreeStateRegistryView {

    override operator fun get(path: WarpPath): WarpState? {
        return warps[path]
    }

    public operator fun set(path: WarpPath, state: WarpState) {
        warps[path] = state
    }

    override operator fun get(path: FolderPath): FolderState? {
        return folders[path]
    }

    public operator fun set(path: FolderPath, state: FolderState) {
        folders[path] = state
    }

    public fun move(src: WarpPath, dst: WarpPath): WarpState? {
        val state = warps[src] ?: return null
        warps[dst] = state
        warps.remove(src)
        return state
    }

    public fun move(src: FolderPath, dst: FolderPath): FolderState? {
        val state = folders[src] ?: return null
        folders[dst] = state
        folders.remove(src)
        return state
    }

    public fun remove(path: WarpPath): WarpState? {
        return warps.remove(path)
    }

    public fun remove(path: FolderPath): FolderState? {
        return folders.remove(path)
    }

    public fun clear() {
        warps.clear()
        folders.clear()
        root = FolderState(Component.text("root"), emptyList(), emptySet(), emptyMap())
    }
}
