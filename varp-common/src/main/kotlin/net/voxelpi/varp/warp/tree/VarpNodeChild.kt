package net.voxelpi.varp.warp.tree

import net.voxelpi.varp.api.DuplicatesStrategy
import net.voxelpi.varp.api.warp.node.NodeChild
import net.voxelpi.varp.api.warp.path.NodeChildPath
import net.voxelpi.varp.api.warp.path.NodeParentPath

interface VarpNodeChild : NodeChild, VarpNode {

    override val parent: VarpNodeParent
        get() = tree.container(path.parent)!!

    override fun copy(destination: NodeParentPath, duplicatesStrategy: DuplicatesStrategy): Result<VarpNodeChild>

    override fun copy(destination: NodeChildPath, duplicatesStrategy: DuplicatesStrategy): Result<VarpNodeChild>
}
