package net.voxelpi.varp.api.warp.tree

import net.voxelpi.varp.api.DuplicatesStrategy
import net.voxelpi.varp.api.warp.path.NodePath
import net.voxelpi.varp.api.warp.state.NodeState

interface Node {

    /**
     * The path to the node.
     */
    val path: NodePath

    /**
     * The id of the node.
     */
    val id: String
        get() = path.id

    /**
     * The state of the node.
     */
    val state: NodeState

    /**
     * The module of the node.
     */
    val module: Module

    /**
     * Delete the node.
     */
    fun delete(): Result<Unit>

    /**
     * Changes the id of the node.
     */
    fun move(id: String, duplicatesStrategy: DuplicatesStrategy): Result<Unit>
}
