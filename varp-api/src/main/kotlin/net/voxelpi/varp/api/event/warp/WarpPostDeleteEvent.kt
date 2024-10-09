package net.voxelpi.varp.api.event.warp

import net.voxelpi.varp.api.event.node.NodePostDeleteEvent
import net.voxelpi.varp.api.warp.path.WarpPath
import net.voxelpi.varp.api.warp.state.WarpState

/**
 * Called when a warp is deleted.
 * @property path the path to the deleted warp.
 * @property state the state of the deleted warp.
 */
@JvmRecord
data class WarpPostDeleteEvent(
    val path: WarpPath,
    val state: WarpState,
) : NodePostDeleteEvent
