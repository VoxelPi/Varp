package net.voxelpi.varp.tree.state

import net.voxelpi.varp.tree.path.FolderPath
import net.voxelpi.varp.tree.path.NodeParentPath
import net.voxelpi.varp.tree.path.NodePath
import net.voxelpi.varp.tree.path.RootPath
import net.voxelpi.varp.tree.path.WarpPath

public interface TreeState {
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

    /**
     * Returns the state of a subtree rooted at [root].
     *
     * @param excludedPaths nodes which should be excluded from the returned tree.
     */
    public fun subtree(
        root: NodeParentPath,
        excludedPaths: Set<NodeParentPath> = emptySet(),
    ): TreeState? {
        if (root in excludedPaths) {
            return null
        }

        val rootState = this[root] ?: return null
        return MutableTreeState(
            warps
                .filterKeys { warpPath -> warpPath.isProperSubpathOf(root) && excludedPaths.none { warpPath.isSubpathOf(it) } }
                .mapKeys { (warpPath, _) -> warpPath.relativeTo(root)!! } // We already filtered out warps that are not under to the root.
                .toMutableMap(),
            folders
                .filterKeys { folderPath -> folderPath.isProperSubpathOf(root) && excludedPaths.none { folderPath.isSubpathOf(it) } }
                .mapKeys { (folderPath, _) -> folderPath.relativeTo(root)!! as FolderPath } // We already filtered out warps that are not under to the root.
                .toMutableMap(),
            rootState,
        )
    }

    public companion object {

        public fun empty(): TreeState {
            return MutableTreeState()
        }
    }
}
