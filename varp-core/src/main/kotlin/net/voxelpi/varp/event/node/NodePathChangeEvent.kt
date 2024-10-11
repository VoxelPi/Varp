package net.voxelpi.varp.event.node

import net.voxelpi.varp.warp.path.NodePath

/**
 * Called when the path of a node is modified.
 * @property newPath the new path of the node.
 * @property oldPath the previous path of the node.
 */
public interface NodePathChangeEvent : NodeEvent {

    public val newPath: NodePath

    public val oldPath: NodePath
}
