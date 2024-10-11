package net.voxelpi.varp.event.warp

import net.voxelpi.varp.event.node.NodePathChangeEvent
import net.voxelpi.varp.warp.Warp
import net.voxelpi.varp.warp.path.WarpPath

/**
 * Called when the path of a warp is modified.
 * @property warp the modified warp.
 * @property newPath the new path of the warp.
 * @property oldPath the previous path of the warp.
 */
@JvmRecord
public data class WarpPathChangeEvent(
    override val warp: Warp,
    override val newPath: WarpPath,
    override val oldPath: WarpPath,
) : WarpEvent, NodePathChangeEvent
