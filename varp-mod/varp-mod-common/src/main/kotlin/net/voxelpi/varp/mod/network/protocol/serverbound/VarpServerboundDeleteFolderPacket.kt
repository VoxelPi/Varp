package net.voxelpi.varp.mod.network.protocol.serverbound

import net.voxelpi.varp.mod.network.protocol.PacketId
import net.voxelpi.varp.tree.path.FolderPath

/**
 * Send by the client to the server to request the deletion of a folder.
 * @property path The path of the folder that should be deleted.
 */
@JvmRecord
@PacketId("delete_folder")
data class VarpServerboundDeleteFolderPacket(
    val path: FolderPath,
) : VarpServerboundPacket
