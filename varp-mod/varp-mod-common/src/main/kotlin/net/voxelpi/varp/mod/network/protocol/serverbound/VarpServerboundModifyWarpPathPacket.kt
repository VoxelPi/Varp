package net.voxelpi.varp.mod.network.protocol.serverbound

import net.voxelpi.varp.mod.network.protocol.PacketId
import net.voxelpi.varp.tree.path.WarpPath

/**
 * Send by the client to the server to request the modification of the path of a warp.
 * @property from The old path of the warp.
 * @property to The new path of the warp.
 */
@JvmRecord
@PacketId("modify_warp_path")
data class VarpServerboundModifyWarpPathPacket(
    val from: WarpPath,
    val to: WarpPath,
) : VarpServerboundPacket
