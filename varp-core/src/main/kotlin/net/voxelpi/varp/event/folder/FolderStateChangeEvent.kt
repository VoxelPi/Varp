package net.voxelpi.varp.event.folder

import net.voxelpi.varp.event.node.NodeStateChangeEvent
import net.voxelpi.varp.warp.Folder
import net.voxelpi.varp.warp.state.FolderState

/**
 * Called when the state of a folder is modified.
 * @property folder the modified folder.
 * @property newState the new state of teh folder.
 * @property oldState the previous state of the folder.
 */
@JvmRecord
public data class FolderStateChangeEvent(
    override val folder: Folder,
    override val newState: FolderState,
    override val oldState: FolderState,
) : FolderEvent, NodeStateChangeEvent
