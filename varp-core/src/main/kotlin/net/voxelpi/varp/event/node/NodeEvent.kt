package net.voxelpi.varp.event.node

import net.voxelpi.varp.event.VarpEvent
import net.voxelpi.varp.tree.Node

/**
 * Base event for all node tree related events.
 */
public interface NodeEvent : VarpEvent {

    /**
     * The affected node.
     */
    public val node: Node
}
