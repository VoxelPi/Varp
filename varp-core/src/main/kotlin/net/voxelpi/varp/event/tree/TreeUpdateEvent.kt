package net.voxelpi.varp.event.tree

import net.voxelpi.varp.tree.Tree
import net.voxelpi.varp.tree.state.TreeState

/**
 * Fired whenever there are major changes to a tree.
 */
@JvmRecord
public data class TreeUpdateEvent(
    override val tree: Tree,
    val previousState: TreeState,
    val newState: TreeState,
) : TreeEvent
