package net.voxelpi.varp.api.warp.tree

import net.voxelpi.varp.api.warp.Tree
import net.voxelpi.varp.api.warp.path.NodePath

interface Node {

    /**
     * The tree this node belongs to.
     */
    val tree: Tree

    /**
     * The path to the node.
     */
    val path: NodePath
}
