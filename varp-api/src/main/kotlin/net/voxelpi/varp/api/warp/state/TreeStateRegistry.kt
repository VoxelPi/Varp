package net.voxelpi.varp.api.warp.state

import net.kyori.adventure.text.Component
import net.voxelpi.varp.api.warp.path.FolderPath
import net.voxelpi.varp.api.warp.path.WarpPath

data class TreeStateRegistry(
    override val warps: MutableMap<WarpPath, WarpState> = mutableMapOf(),
    override val folders: MutableMap<FolderPath, FolderState> = mutableMapOf(),
    override var root: FolderState = FolderState(Component.empty(), emptyList(), emptySet(), emptyMap()),
) : TreeStateRegistryView {

    override operator fun get(path: WarpPath): WarpState? {
        return warps[path]
    }

    operator fun set(path: WarpPath, state: WarpState) {
        warps[path] = state
    }

    override operator fun get(path: FolderPath): FolderState? {
        return folders[path]
    }

    operator fun set(path: FolderPath, state: FolderState) {
        folders[path] = state
    }

    fun move(src: WarpPath, dst: WarpPath): WarpState? {
        val state = warps[src] ?: return null
        warps[dst] = state
        warps.remove(src)
        return state
    }

    fun move(src: FolderPath, dst: FolderPath): FolderState? {
        val state = folders[src] ?: return null
        folders[dst] = state
        folders.remove(src)
        return state
    }

    fun remove(path: WarpPath): WarpState? {
        return warps.remove(path)
    }

    fun remove(path: FolderPath): FolderState? {
        return folders.remove(path)
    }
}
