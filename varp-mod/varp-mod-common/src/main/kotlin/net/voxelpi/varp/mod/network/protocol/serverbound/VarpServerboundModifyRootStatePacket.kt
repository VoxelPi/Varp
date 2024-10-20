package net.voxelpi.varp.mod.network.protocol.serverbound

import net.voxelpi.varp.mod.network.protocol.PacketId
import net.voxelpi.varp.warp.state.FolderState

/**
 * Send by the client to the server to request the modification of the state of the root folder.
 * @property state The new state of the root that should be modified.
 */
@JvmRecord
@PacketId("modify_root_state")
data class VarpServerboundModifyRootStatePacket(
    val state: FolderState,
) : VarpServerboundPacket
