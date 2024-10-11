package net.voxelpi.varp.api.warp

import net.voxelpi.varp.api.warp.node.NodeParent
import net.voxelpi.varp.api.warp.path.RootPath
import net.voxelpi.varp.api.warp.state.FolderState

interface Root : NodeParent {

    /**
     * The path of the root folder.
     */
    override val path: RootPath

    /**
     * The state of the root folder.
     */
    override val state: FolderState
}
