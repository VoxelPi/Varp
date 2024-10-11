package net.voxelpi.varp.warp.state

import net.voxelpi.varp.warp.path.FolderPath
import net.voxelpi.varp.warp.path.WarpPath

public interface TreeStateRegistryView {
    public val warps: Map<WarpPath, WarpState>
    public val folders: Map<FolderPath, FolderState>
    public val root: FolderState

    public operator fun get(path: WarpPath): WarpState? {
        return warps[path]
    }

    public operator fun get(path: FolderPath): FolderState? {
        return folders[path]
    }
}
