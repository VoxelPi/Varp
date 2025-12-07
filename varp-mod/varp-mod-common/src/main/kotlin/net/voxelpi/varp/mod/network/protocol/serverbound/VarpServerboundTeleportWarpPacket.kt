package net.voxelpi.varp.mod.network.protocol.serverbound

import net.voxelpi.varp.mod.network.protocol.PacketId
import net.voxelpi.varp.tree.path.WarpPath

/**
 * Send by the client to the server to request teleportation to the given warp.
 * @property path The path of the warp that the client wants to teleport to.
 */
@JvmRecord
@PacketId("teleport_warp")
data class VarpServerboundTeleportWarpPacket(
    val path: WarpPath,
) : VarpServerboundPacket
