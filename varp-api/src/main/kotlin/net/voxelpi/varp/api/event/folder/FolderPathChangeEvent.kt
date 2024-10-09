package net.voxelpi.varp.api.event.folder

import net.voxelpi.varp.api.event.node.NodePathChangeEvent
import net.voxelpi.varp.api.warp.Folder
import net.voxelpi.varp.api.warp.path.FolderPath

/**
 * Called when the path of a folder is modified.
 * @property folder the modified folder.
 * @property newPath the new path of the folder.
 * @property oldPath the previous path of the folder.
 */
@JvmRecord
data class FolderPathChangeEvent(
    override val folder: Folder,
    override val newPath: FolderPath,
    override val oldPath: FolderPath,
) : FolderEvent, NodePathChangeEvent
