package net.voxelpi.varp.event.folder

import net.voxelpi.varp.event.node.NodeCreateEvent
import net.voxelpi.varp.tree.Folder

/**
 * Called when a folder is created.
 * @property folder the created folder.
 */
@JvmRecord
public data class FolderCreateEvent(
    override val folder: Folder,
) : FolderEvent, NodeCreateEvent
