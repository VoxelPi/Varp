package net.voxelpi.varp.event.root

import net.voxelpi.varp.event.node.NodeEvent
import net.voxelpi.varp.tree.Root

/**
 * Base event for all root related events.
 */
public interface RootEvent : NodeEvent {

    /**
     * The affected module.
     */
    public val root: Root

    override val node: Root
        get() = root
}
