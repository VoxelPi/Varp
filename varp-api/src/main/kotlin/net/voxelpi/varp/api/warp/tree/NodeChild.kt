package net.voxelpi.varp.api.warp.tree

import net.voxelpi.varp.api.DuplicatesStrategy
import net.voxelpi.varp.api.warp.path.NodeChildPath
import net.voxelpi.varp.api.warp.path.NodeParentPath

interface NodeChild : Node {

    override val path: NodeChildPath

    /**
     * The parent node of this node.
     */
    val parent: NodeParent

    /**
     * Moves the node the given path.
     */
    fun move(destination: NodeParentPath, duplicatesStrategy: DuplicatesStrategy): Result<Unit>

    /**
     * Moves the node the given path and changes its id.
     */
    fun move(destination: NodeChildPath, duplicatesStrategy: DuplicatesStrategy): Result<Unit>

    /**
     * Copies the node the given path.
     * @return the created node.
     */
    fun copy(destination: NodeParentPath, duplicatesStrategy: DuplicatesStrategy): Result<NodeChild>

    /**
     * Copies the node the given path and changes its id.
     * @return the created node.
     */
    fun copy(destination: NodeChildPath, duplicatesStrategy: DuplicatesStrategy): Result<NodeChild>
}
