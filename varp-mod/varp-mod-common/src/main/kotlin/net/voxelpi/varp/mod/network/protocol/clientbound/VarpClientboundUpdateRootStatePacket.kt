package net.voxelpi.varp.mod.network.protocol.clientbound

import net.voxelpi.varp.mod.network.protocol.PacketId
import net.voxelpi.varp.tree.state.FolderState

/**
 * Send by the server to the client to inform the client about a change in the state of the root folder.
 * @property state The new state of the root.
 */
@JvmRecord
@PacketId("update_root_state")
data class VarpClientboundUpdateRootStatePacket(
    val state: FolderState,
) : VarpClientboundPacket
