package net.voxelpi.varp.tree

import net.voxelpi.varp.DuplicatesStrategy
import net.voxelpi.varp.tree.path.NodeChildPath
import net.voxelpi.varp.tree.path.NodeParentPath
import net.voxelpi.varp.tree.state.NodeState

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
        get() = tree[path.parent]!!

    /**
     * Changes the id of the node.
     */
    public suspend fun move(
        id: String,
        duplicatesStrategy: DuplicatesStrategy = DuplicatesStrategy.FAIL,
    ): Result<Unit>

    /**
     * Moves the node the given path.
     */
    public suspend fun moveInto(
        parent: NodeParentPath,
        id: String? = null,
        duplicatesStrategy: DuplicatesStrategy = DuplicatesStrategy.FAIL,
    ): Result<Unit>

    /**
     * Copies the node the given path.
     * @return the created node.
     */
    public suspend fun copyInto(
        parent: NodeParentPath,
        id: String? = null,
        duplicatesStrategy: DuplicatesStrategy = DuplicatesStrategy.FAIL,
    ): Result<NodeChild>

    /**
     * Delete the node.
     */
    public suspend fun delete(): Result<NodeState>
}
