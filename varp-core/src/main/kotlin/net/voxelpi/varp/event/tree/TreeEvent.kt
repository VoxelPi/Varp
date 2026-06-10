package net.voxelpi.varp.event.tree

import net.voxelpi.varp.event.VarpEvent
import net.voxelpi.varp.tree.Tree

/**
 * Base interface for all tree related events.
 */
public interface TreeEvent : VarpEvent {

    /**
     * The affected tree.
     */
    public val tree: Tree
}
