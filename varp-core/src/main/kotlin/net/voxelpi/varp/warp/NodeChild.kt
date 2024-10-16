package net.voxelpi.varp.warp

import net.voxelpi.varp.DuplicatesStrategy
import net.voxelpi.varp.warp.path.NodeChildPath
import net.voxelpi.varp.warp.path.NodeParentPath

public sealed interface NodeChild : Node {

    override val path: NodeChildPath

    /**
     * The id of the node.
     */
    public val id: String
        get() = path.id

    /**
     * The parent node of this node.
     */
    public val parent: NodeParent
        get() = tree.resolve(path.parent)!!

    /**
     * Changes the id of the node.
     */
    public fun move(id: String, duplicatesStrategy: DuplicatesStrategy): Result<Unit>

    /**
     * Moves the node the given path.
     */
    public fun move(destination: NodeParentPath, duplicatesStrategy: DuplicatesStrategy, destinationId: String? = null): Result<Unit>

    /**
     * Copies the node the given path.
     * @return the created node.
     */
    public fun copy(destination: NodeParentPath, duplicatesStrategy: DuplicatesStrategy, destinationId: String? = null): Result<NodeChild>

    /**
     * Delete the node.
     */
    public fun delete(): Result<Unit>
}
