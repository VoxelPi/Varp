package net.voxelpi.varp.mod.network.protocol.serverbound

import net.voxelpi.varp.mod.network.protocol.PacketId
import net.voxelpi.varp.tree.path.FolderPath
import net.voxelpi.varp.tree.state.FolderState

/**
 * Send by the client to the server to request the modification of the state of a folder.
 * @property path The path of the folder that should be modified.
 * @property state The new state of the folder that should be modified.
 */
@JvmRecord
@PacketId("modify_folder_state")
data class VarpServerboundModifyFolderStatePacket(
    val path: FolderPath,
    val state: FolderState,
) : VarpServerboundPacket
