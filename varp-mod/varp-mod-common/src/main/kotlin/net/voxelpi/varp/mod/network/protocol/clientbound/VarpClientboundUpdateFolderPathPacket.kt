package net.voxelpi.varp.mod.network.protocol.clientbound

import net.voxelpi.varp.mod.network.protocol.PacketId
import net.voxelpi.varp.tree.path.FolderPath

/**
 * Send by the server to the client to inform the client about a change in the path of a folder.
 * @property from The old path of the folder.
 * @property to The new path of the folder.
 */
@JvmRecord
@PacketId("update_folder_path")
data class VarpClientboundUpdateFolderPathPacket(
    val from: FolderPath,
    val to: FolderPath,
) : VarpClientboundPacket
