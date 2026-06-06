package net.voxelpi.varp.event.node

import net.voxelpi.varp.tree.state.FolderState

public interface NodeParentStateChangeEvent : NodeParentEvent, NodeStateChangeEvent {

    override val newState: FolderState

    override val oldState: FolderState
}
