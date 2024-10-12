package net.voxelpi.varp.warp

import net.voxelpi.varp.warp.path.NodePath
import net.voxelpi.varp.warp.state.NodeState

public sealed interface Node {

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
