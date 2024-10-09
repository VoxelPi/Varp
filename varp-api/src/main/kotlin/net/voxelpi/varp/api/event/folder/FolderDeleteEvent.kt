package net.voxelpi.varp.api.event.folder

import net.voxelpi.varp.api.event.node.NodeDeleteEvent
import net.voxelpi.varp.api.warp.Folder

/**
 * Called when a folder is deleted.
 * @property folder the deleted folder.
 */
@JvmRecord
data class FolderDeleteEvent(
    override val folder: Folder,
) : FolderEvent, NodeDeleteEvent
