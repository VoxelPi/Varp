package net.voxelpi.varp.event.node

import net.voxelpi.varp.tree.NodeParent

public interface NodeParentEvent : NodeEvent {

    override val node: NodeParent
}
