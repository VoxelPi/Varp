package net.voxelpi.varp.warp.tree

import net.voxelpi.varp.api.warp.node.Node
import net.voxelpi.varp.warp.VarpTree

interface VarpNode : Node {

    override val tree: VarpTree
}
