package net.voxelpi.varp.event.node

import net.voxelpi.varp.event.VarpEvent
import net.voxelpi.varp.warp.path.NodeChildPath
import net.voxelpi.varp.warp.state.NodeState

/**
 * Called after a node has been deleted.
 */
public interface NodePostDeleteEvent : VarpEvent {

    /**
     * The path of the deleted node.
     */
    public val path: NodeChildPath

    /**
     * The state of the deleted node.
     */
    public val state: NodeState
}
