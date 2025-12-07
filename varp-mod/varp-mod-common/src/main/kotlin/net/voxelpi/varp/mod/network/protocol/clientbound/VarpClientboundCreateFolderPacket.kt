package net.voxelpi.varp.mod.network.protocol.clientbound

import net.voxelpi.varp.mod.network.protocol.PacketId
import net.voxelpi.varp.tree.path.FolderPath
import net.voxelpi.varp.tree.state.FolderState

/**
 * Send by the server to the client to inform the client about the creation of a folder.
 * @property path The path of the created folder.
 * @property state The state of the created folder.
 */
@JvmRecord
@PacketId("create_folder")
data class VarpClientboundCreateFolderPacket(
    val path: FolderPath,
    val state: FolderState,
) : VarpClientboundPacket
