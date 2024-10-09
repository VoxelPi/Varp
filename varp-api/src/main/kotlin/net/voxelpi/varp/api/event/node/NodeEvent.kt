package net.voxelpi.varp.api.event.node

import net.voxelpi.varp.api.event.VarpEvent
import net.voxelpi.varp.api.warp.node.Node

/**
 * Base event for all node tree related events.
 */
interface NodeEvent : VarpEvent {

    /**
     * The affected node.
     */
    val node: Node
}
