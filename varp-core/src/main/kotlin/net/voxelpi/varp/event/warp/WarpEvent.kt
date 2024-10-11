package net.voxelpi.varp.event.warp

import net.voxelpi.varp.event.node.NodeEvent
import net.voxelpi.varp.warp.Warp

/**
 * Base event for all warp related events.
 */
public interface WarpEvent : NodeEvent {

    /**
     * The affected warp.
     */
    public val warp: Warp

    override val node: Warp
        get() = warp
}
