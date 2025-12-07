package net.voxelpi.varp.event.warp

import net.voxelpi.varp.event.node.NodeCreateEvent
import net.voxelpi.varp.tree.Warp

/**
 * Called when a warp is created.
 * @property warp the created warp.
 */
@JvmRecord
public data class WarpCreateEvent(
    override val warp: Warp,
) : WarpEvent, NodeCreateEvent
