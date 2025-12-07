package net.voxelpi.varp.mod.network.protocol.serverbound

import net.voxelpi.varp.mod.network.protocol.PacketId
import net.voxelpi.varp.tree.path.WarpPath
import net.voxelpi.varp.tree.state.WarpState

/**
 * Send by the client to the server to request the modification of the state of a warp.
 * @property path The path of the warp that should be modified.
 * @property state The new state of the warp that should be modified.
 */
@JvmRecord
@PacketId("modify_warp_state")
data class VarpServerboundModifyWarpStatePacket(
    val path: WarpPath,
    val state: WarpState,
) : VarpServerboundPacket
