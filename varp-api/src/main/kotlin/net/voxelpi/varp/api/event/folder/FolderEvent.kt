package net.voxelpi.varp.api.event.folder

import net.voxelpi.varp.api.event.node.NodeEvent
import net.voxelpi.varp.api.warp.Folder

/**
 * Base event for all folder related events.
 */
interface FolderEvent : NodeEvent {

    /**
     * The affected folder.
     */
    val folder: Folder

    override val node: Folder
        get() = folder
}
