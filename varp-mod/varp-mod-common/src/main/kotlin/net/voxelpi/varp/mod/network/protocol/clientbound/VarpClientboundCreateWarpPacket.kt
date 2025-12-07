package net.voxelpi.varp.mod.network.protocol.clientbound

import net.voxelpi.varp.mod.network.protocol.PacketId
import net.voxelpi.varp.tree.path.WarpPath
import net.voxelpi.varp.tree.state.WarpState

/**
 * Send by the server to the client to inform the client about the creation of a warp.
 * @property path The path of the created warp.
 * @property state The state of the created warp.
 */
@JvmRecord
@PacketId("create_warp")
data class VarpClientboundCreateWarpPacket(
    val path: WarpPath,
    val state: WarpState,
) : VarpClientboundPacket
