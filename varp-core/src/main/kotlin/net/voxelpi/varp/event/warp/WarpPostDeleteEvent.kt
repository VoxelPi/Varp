package net.voxelpi.varp.event.warp

import net.voxelpi.varp.event.node.NodePostDeleteEvent
import net.voxelpi.varp.warp.path.WarpPath
import net.voxelpi.varp.warp.state.WarpState

/**
 * Called when a warp is deleted.
 * @property path the path to the deleted warp.
 * @property state the state of the deleted warp.
 */
@JvmRecord
public data class WarpPostDeleteEvent(
    override val path: WarpPath,
    override val state: WarpState,
) : NodePostDeleteEvent
