package net.voxelpi.varp.mod.network.protocol.serverbound

import net.voxelpi.varp.mod.network.protocol.PacketId
import net.voxelpi.varp.warp.path.FolderPath

/**
 * Send by the client to the server to request the modification of the path of a folder.
 * @property from The old path of the folder.
 * @property to The new path of the folder.
 */
@JvmRecord
@PacketId("modify_folder_path")
data class VarpServerboundModifyFolderPathPacket(
    val from: FolderPath,
    val to: FolderPath,
) : VarpServerboundPacket
