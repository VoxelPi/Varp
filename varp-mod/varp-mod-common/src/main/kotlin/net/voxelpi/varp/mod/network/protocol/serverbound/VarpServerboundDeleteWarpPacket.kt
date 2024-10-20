package net.voxelpi.varp.mod.network.protocol.serverbound

import net.voxelpi.varp.mod.network.protocol.PacketId
import net.voxelpi.varp.warp.path.WarpPath

/**
 * Send by the client to the server to request the deletion of a warp.
 * @property path The path of the warp that should be deleted.
 */
@JvmRecord
@PacketId("delete_warp")
data class VarpServerboundDeleteWarpPacket(
    val path: WarpPath,
) : VarpServerboundPacket
