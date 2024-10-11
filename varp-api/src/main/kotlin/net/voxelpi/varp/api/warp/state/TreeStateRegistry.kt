package net.voxelpi.varp.api.warp.state

import net.kyori.adventure.text.Component
import net.voxelpi.varp.api.warp.path.FolderPath
import net.voxelpi.varp.api.warp.path.WarpPath

data class TreeStateRegistry(
    override val warps: MutableMap<WarpPath, WarpState> = mutableMapOf(),
    override val folders: MutableMap<FolderPath, FolderState> = mutableMapOf(),
    override var root: RootState = RootState(Component.empty(), emptyList(), emptySet(), emptyMap()),
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

    fun remove(path: WarpPath): WarpState? {
        return warps.remove(path)
    }

    fun remove(path: FolderPath): FolderState? {
        return folders.remove(path)
    }
}
