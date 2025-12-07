package net.voxelpi.varp.tree

import net.voxelpi.varp.option.OptionValue
import net.voxelpi.varp.tree.path.NodeChildPath
import net.voxelpi.varp.tree.path.NodeParentPath

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
    public suspend fun move(id: String, options: Collection<OptionValue<*>> = emptyList()): Result<Unit>

    /**
     * Moves the node the given path.
     */
    public suspend fun move(destination: NodeParentPath, destinationId: String? = null, options: Collection<OptionValue<*>> = emptyList()): Result<Unit>

    /**
     * Copies the node the given path.
     * @return the created node.
     */
    public suspend fun copy(destination: NodeParentPath, destinationId: String? = null, options: Collection<OptionValue<*>> = emptyList()): Result<NodeChild>

    /**
     * Delete the node.
     */
    public suspend fun delete(): Result<Unit>
}
