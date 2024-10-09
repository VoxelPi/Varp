package net.voxelpi.varp.api.event.node

import net.voxelpi.varp.api.warp.state.NodeState

/**
 * Called when the state of a node is modified.
 * @property newState the new state of the node.
 * @property oldState the previous state of the node.
 */
interface NodeStateChangeEvent : NodeEvent {

    val newState: NodeState

    val oldState: NodeState
}
