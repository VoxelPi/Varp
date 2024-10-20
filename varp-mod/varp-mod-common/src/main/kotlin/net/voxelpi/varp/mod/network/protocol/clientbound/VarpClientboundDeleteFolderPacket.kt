package net.voxelpi.varp.mod.network.protocol.clientbound

import net.voxelpi.varp.mod.network.protocol.PacketId
import net.voxelpi.varp.warp.path.FolderPath

/**
 * Send by the server to the client to inform the client about the deletion of a folder.
 * @property path The path of the deleted folder.
 */
@JvmRecord
@PacketId("delete_folder")
data class VarpClientboundDeleteFolderPacket(
    val path: FolderPath,
) : VarpClientboundPacket
