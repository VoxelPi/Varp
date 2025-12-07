package net.voxelpi.varp.tree.state

import net.voxelpi.varp.tree.path.FolderPath
import net.voxelpi.varp.tree.path.NodeParentPath
import net.voxelpi.varp.tree.path.NodePath
import net.voxelpi.varp.tree.path.RootPath
import net.voxelpi.varp.tree.path.WarpPath

public interface TreeStateRegistryView {
    public val warps: Map<WarpPath, WarpState>
    public val folders: Map<FolderPath, FolderState>
    public val root: FolderState

    /**
     * Checks whether the registry contains a node with the given [path].
     */
    public operator fun contains(path: NodePath): Boolean {
        return when (path) {
            is FolderPath -> path in folders
            is WarpPath -> path in warps
            RootPath -> true
        }
    }

    public operator fun get(path: WarpPath): WarpState? {
        return warps[path]
    }

    public operator fun get(path: FolderPath): FolderState? {
        return folders[path]
    }

    public operator fun get(path: NodeParentPath): FolderState? {
        return when (path) {
            is FolderPath -> folders[path]
            RootPath -> root
        }
    }
}
