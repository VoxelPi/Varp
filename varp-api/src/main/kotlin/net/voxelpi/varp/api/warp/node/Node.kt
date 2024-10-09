package net.voxelpi.varp.api.warp.node

import net.voxelpi.varp.api.warp.Tree
import net.voxelpi.varp.api.warp.path.NodePath
import net.voxelpi.varp.api.warp.state.NodeState

interface Node {

    /**
     * The tree this node belongs to.
     */
    val tree: Tree

    /**
     * The path to the node.
     */
    val path: NodePath

    /**
     * The state of the node.
     */
    val state: NodeState
}
