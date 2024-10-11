package net.voxelpi.varp.warp

import net.voxelpi.varp.warp.node.NodeParent
import net.voxelpi.varp.warp.path.RootPath
import net.voxelpi.varp.warp.state.FolderState

public class Root internal constructor(
    override val tree: Tree,
) : NodeParent {

    /**
     * The path of the root folder.
     */
    override val path: RootPath
        get() = RootPath

    /**
     * The state of the root folder.
     */
    override val state: FolderState
        get() = tree.rootState()
}
