package net.voxelpi.varp.event.warp

import net.voxelpi.varp.event.node.NodeDeleteEvent
import net.voxelpi.varp.warp.Warp

/**
 * Called when a warp is deleted.
 * @property warp the deleted warp.
 */
@JvmRecord
public data class WarpDeleteEvent(
    override val warp: Warp,
) : WarpEvent, NodeDeleteEvent
