package net.voxelpi.varp.mod.network.protocol.clientbound

import net.voxelpi.varp.mod.network.protocol.PacketId
import net.voxelpi.varp.warp.path.FolderPath
import net.voxelpi.varp.warp.state.FolderState

/**
 * Send by the server to the client to inform the client about a change in the state of a folder.
 * @property path The path of the changed folder.
 * @property state The new state of the folder.
 */
@JvmRecord
@PacketId("update_folder_state")
data class VarpClientboundUpdateFolderStatePacket(
    val path: FolderPath,
    val state: FolderState,
) : VarpClientboundPacket
