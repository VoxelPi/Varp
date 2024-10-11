package net.voxelpi.varp.event.warp

import net.voxelpi.varp.event.node.NodeStateChangeEvent
import net.voxelpi.varp.warp.Warp
import net.voxelpi.varp.warp.state.WarpState

/**
 * Called when the state of a warp is modified.
 * @property warp the modified warp.
 * @property newState the new state of the warp.
 * @property oldState the previous state of the warp.
 */
@JvmRecord
public data class WarpStateChangeEvent(
    override val warp: Warp,
    override val newState: WarpState,
    override val oldState: WarpState,
) : WarpEvent, NodeStateChangeEvent
