package net.voxelpi.varp.warp.node

import net.voxelpi.varp.warp.Tree
import net.voxelpi.varp.warp.path.NodePath
import net.voxelpi.varp.warp.state.NodeState

public interface Node {

    /**
     * The tree this node belongs to.
     */
    public val tree: Tree

    /**
     * The path to the node.
     */
    public val path: NodePath

    /**
     * The state of the node.
     */
    public val state: NodeState
}
