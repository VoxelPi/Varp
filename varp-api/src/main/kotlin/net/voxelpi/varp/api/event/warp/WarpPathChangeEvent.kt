package net.voxelpi.varp.api.event.warp

import net.voxelpi.varp.api.event.node.NodePathChangeEvent
import net.voxelpi.varp.api.warp.Warp
import net.voxelpi.varp.api.warp.path.WarpPath

/**
 * Called when the path of a warp is modified.
 * @property warp the modified warp.
 * @property newPath the new path of the warp.
 * @property oldPath the previous path of the warp.
 */
@JvmRecord
data class WarpPathChangeEvent(
    override val warp: Warp,
    override val newPath: WarpPath,
    override val oldPath: WarpPath,
) : WarpEvent, NodePathChangeEvent
