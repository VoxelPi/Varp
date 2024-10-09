package net.voxelpi.varp.api.event.warp

import net.voxelpi.varp.api.event.node.NodeCreateEvent
import net.voxelpi.varp.api.warp.Warp

/**
 * Called when a warp is created.
 * @property warp the created warp.
 */
@JvmRecord
data class WarpCreateEvent(
    override val warp: Warp,
) : WarpEvent, NodeCreateEvent
