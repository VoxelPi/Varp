package net.voxelpi.varp.api.event.node

import net.voxelpi.varp.api.warp.path.NodePath

/**
 * Called when the path of a node is modified.
 * @property newPath the new path of the node.
 * @property oldPath the previous path of the node.
 */
interface NodePathChangeEvent : NodeEvent {

    val newPath: NodePath

    val oldPath: NodePath
}
