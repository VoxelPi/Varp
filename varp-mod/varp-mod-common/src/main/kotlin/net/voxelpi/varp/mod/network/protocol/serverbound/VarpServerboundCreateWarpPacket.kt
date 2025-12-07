package net.voxelpi.varp.mod.network.protocol.serverbound

import net.voxelpi.varp.mod.network.protocol.PacketId
import net.voxelpi.varp.tree.path.WarpPath
import net.voxelpi.varp.tree.state.WarpState

/**
 * Send by the client to the server to request the creation of a warp.
 * @property path The path of the warp that should be created.
 * @property state The state of the warp that should be created.
 */
@JvmRecord
@PacketId("create_warp")
data class VarpServerboundCreateWarpPacket(
    val path: WarpPath,
    val state: WarpState,
) : VarpServerboundPacket
