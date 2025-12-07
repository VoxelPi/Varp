package net.voxelpi.varp.mod.network.protocol.clientbound

import net.voxelpi.varp.mod.network.protocol.PacketId
import net.voxelpi.varp.tree.path.WarpPath

/**
 * Send by the server to the client to inform the client about the deletion of a warp.
 * @property path The path of the deleted warp.
 */
@JvmRecord
@PacketId("delete_warp")
data class VarpClientboundDeleteWarpPacket(
    val path: WarpPath,
) : VarpClientboundPacket
