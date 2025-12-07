package net.voxelpi.varp.mod.network.protocol.clientbound

import net.voxelpi.varp.mod.network.protocol.PacketId
import net.voxelpi.varp.tree.path.WarpPath
import net.voxelpi.varp.tree.state.WarpState

/**
 * Send by the server to the client to inform the client about a change in the state of a warp.
 * @property path The path of the changed warp.
 * @property state The new state of the warp.
 */
@JvmRecord
@PacketId("update_warp_state")
data class VarpClientboundUpdateWarpStatePacket(
    val path: WarpPath,
    val state: WarpState,
) : VarpClientboundPacket
