package net.voxelpi.varp.api.event.folder

import net.voxelpi.varp.api.event.node.NodePostDeleteEvent
import net.voxelpi.varp.api.warp.path.FolderPath
import net.voxelpi.varp.api.warp.state.FolderState

/**
 * Called after a folder has been deleted.
 * @property path the path to the deleted folder.
 * @property state the state of the deleted folder.
 */
@JvmRecord
data class FolderPostDeleteEvent(
    val path: FolderPath,
    val state: FolderState,
) : NodePostDeleteEvent
