package net.voxelpi.varp.api.event.warp

import net.voxelpi.varp.api.event.node.NodeDeleteEvent
import net.voxelpi.varp.api.warp.Warp

/**
 * Called when a warp is deleted.
 * @property warp the deleted warp.
 */
@JvmRecord
data class WarpDeleteEvent(
    override val warp: Warp,
) : WarpEvent, NodeDeleteEvent
