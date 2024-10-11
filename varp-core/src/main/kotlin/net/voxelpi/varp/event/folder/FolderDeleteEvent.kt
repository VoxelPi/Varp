package net.voxelpi.varp.event.folder

import net.voxelpi.varp.event.node.NodeDeleteEvent
import net.voxelpi.varp.warp.Folder

/**
 * Called when a folder is deleted.
 * @property folder the deleted folder.
 */
@JvmRecord
public data class FolderDeleteEvent(
    override val folder: Folder,
) : FolderEvent, NodeDeleteEvent
