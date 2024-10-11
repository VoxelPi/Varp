package net.voxelpi.varp.event.node

import net.voxelpi.varp.warp.state.NodeState

/**
 * Called when the state of a node is modified.
 * @property newState the new state of the node.
 * @property oldState the previous state of the node.
 */
public interface NodeStateChangeEvent : NodeEvent {

    public val newState: NodeState

    public val oldState: NodeState
}
