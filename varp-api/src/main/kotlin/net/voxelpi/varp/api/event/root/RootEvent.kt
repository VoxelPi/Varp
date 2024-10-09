package net.voxelpi.varp.api.event.root

import net.voxelpi.varp.api.event.node.NodeEvent
import net.voxelpi.varp.api.warp.Root

/**
 * Base event for all root related events.
 */
interface RootEvent : NodeEvent {

    /**
     * The affected module.
     */
    val root: Root

    override val node: Root
        get() = root
}
