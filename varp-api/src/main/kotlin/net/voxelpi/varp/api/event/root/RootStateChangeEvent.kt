package net.voxelpi.varp.api.event.root

import net.voxelpi.varp.api.event.node.NodeStateChangeEvent
import net.voxelpi.varp.api.warp.Root
import net.voxelpi.varp.api.warp.state.FolderState

/**
 * Called when the state of the root is modified.
 * @property root the modified root.
 * @property newState the new state of the module.
 * @property oldState the previous state of the module.
 */
@JvmRecord
data class RootStateChangeEvent(
    override val root: Root,
    override val newState: FolderState,
    override val oldState: FolderState,
) : RootEvent, NodeStateChangeEvent
