package net.voxelpi.varp.event.folder

import net.voxelpi.varp.event.node.NodeEvent
import net.voxelpi.varp.tree.Folder

/**
 * Base event for all folder related events.
 */
public interface FolderEvent : NodeEvent {

    /**
     * The affected folder.
     */
    public val folder: Folder

    override val node: Folder
        get() = folder
}
