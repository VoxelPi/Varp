package net.voxelpi.varp.api.warp.node

import net.voxelpi.varp.api.DuplicatesStrategy
import net.voxelpi.varp.api.warp.path.NodeChildPath
import net.voxelpi.varp.api.warp.path.NodeParentPath

interface NodeChild : Node {

    override val path: NodeChildPath

    /**
     * The id of the node.
     */
    val id: String
        get() = path.id

    /**
     * The parent node of this node.
     */
    val parent: NodeParent

    /**
     * Changes the id of the node.
     */
    fun move(id: String, duplicatesStrategy: DuplicatesStrategy): Result<Unit>

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

    /**
     * Delete the node.
     */
    fun delete(): Result<Unit>
}
