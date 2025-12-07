package net.voxelpi.varp.event.folder

import net.voxelpi.varp.event.node.NodePostDeleteEvent
import net.voxelpi.varp.tree.path.FolderPath
import net.voxelpi.varp.tree.state.FolderState

/**
 * Called after a folder has been deleted.
 * @property path the path to the deleted folder.
 * @property state the state of the deleted folder.
 */
@JvmRecord
public data class FolderPostDeleteEvent(
    override val path: FolderPath,
    override val state: FolderState,
) : NodePostDeleteEvent
