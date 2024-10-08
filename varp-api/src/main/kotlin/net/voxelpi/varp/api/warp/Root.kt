package net.voxelpi.varp.api.warp

import net.voxelpi.varp.api.warp.path.RootPath
import net.voxelpi.varp.api.warp.tree.NodeParent

interface Root : NodeParent {

    /**
     * The path to the module.
     */
    override val path: RootPath
}
