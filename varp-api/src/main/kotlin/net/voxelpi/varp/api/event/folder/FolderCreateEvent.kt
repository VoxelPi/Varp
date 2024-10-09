package net.voxelpi.varp.api.event.folder

import net.voxelpi.varp.api.event.node.NodeCreateEvent
import net.voxelpi.varp.api.warp.Folder

/**
 * Called when a folder is created.
 * @property folder the created folder.
 */
@JvmRecord
data class FolderCreateEvent(
    override val folder: Folder,
) : FolderEvent, NodeCreateEvent
