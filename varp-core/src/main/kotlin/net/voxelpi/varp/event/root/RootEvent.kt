package net.voxelpi.varp.event.root

import net.voxelpi.varp.event.node.NodeParentEvent
import net.voxelpi.varp.tree.Root

/**
 * Base event for all root related events.
 */
public interface RootEvent : NodeParentEvent {

    /**
     * The affected module.
     */
    public val root: Root

    override val node: Root
        get() = root
}
