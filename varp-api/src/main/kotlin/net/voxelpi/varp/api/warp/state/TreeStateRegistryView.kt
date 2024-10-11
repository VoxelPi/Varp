package net.voxelpi.varp.api.warp.state

import net.voxelpi.varp.api.warp.path.FolderPath
import net.voxelpi.varp.api.warp.path.WarpPath

interface TreeStateRegistryView {
    val warps: Map<WarpPath, WarpState>
    val folders: Map<FolderPath, FolderState>
    val root: RootState

    operator fun get(path: WarpPath): WarpState? {
        return warps[path]
    }

    operator fun get(path: FolderPath): FolderState? {
        return folders[path]
    }
}
