package net.voxelpi.varp.warp

import net.kyori.adventure.text.Component
import net.voxelpi.varp.api.warp.path.FolderPath
import net.voxelpi.varp.api.warp.path.WarpPath
import net.voxelpi.varp.api.warp.state.FolderState
import net.voxelpi.varp.api.warp.state.RootState
import net.voxelpi.varp.api.warp.state.WarpState

class VarpTreeStateRegistry {
    val warps: MutableMap<WarpPath, WarpState> = mutableMapOf()
    val folders: MutableMap<FolderPath, FolderState> = mutableMapOf()
    var root: RootState = RootState(Component.empty(), emptyList(), emptySet(), emptyMap())

    operator fun get(path: WarpPath): WarpState? {
        return warps[path]
    }

    operator fun set(path: WarpPath, state: WarpState) {
        warps[path] = state
    }

    operator fun get(path: FolderPath): FolderState? {
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
