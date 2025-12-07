package net.voxelpi.varp.mod.network.protocol.clientbound

import net.voxelpi.varp.mod.network.protocol.PacketId
import net.voxelpi.varp.tree.path.WarpPath

/**
 * Send by the server to the client to inform the client about a change in the path of a warp.
 * @property from The old path of the warp.
 * @property to The new path of the warp.
 */
@JvmRecord
@PacketId("update_warp_path")
data class VarpClientboundUpdateWarpPathPacket(
    val from: WarpPath,
    val to: WarpPath,
) : VarpClientboundPacket
