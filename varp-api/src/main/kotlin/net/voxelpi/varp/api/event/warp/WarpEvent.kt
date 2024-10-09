package net.voxelpi.varp.api.event.warp

import net.voxelpi.varp.api.event.node.NodeEvent
import net.voxelpi.varp.api.warp.Warp

/**
 * Base event for all warp related events.
 */
interface WarpEvent : NodeEvent {

    /**
     * The affected warp.
     */
    val warp: Warp

    override val node: Warp
        get() = warp
}
