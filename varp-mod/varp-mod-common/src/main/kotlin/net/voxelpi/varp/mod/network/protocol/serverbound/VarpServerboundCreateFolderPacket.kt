package net.voxelpi.varp.mod.network.protocol.serverbound

import net.voxelpi.varp.mod.network.protocol.PacketId
import net.voxelpi.varp.warp.path.FolderPath
import net.voxelpi.varp.warp.state.FolderState

/**
 * Send by the client to the server to request the creation of a folder.
 * @property path The path of the folder that should be created.
 * @property state The state of the folder that should be created.
 */
@JvmRecord
@PacketId("create_folder")
data class VarpServerboundCreateFolderPacket(
    val path: FolderPath,
    val state: FolderState,
) : VarpServerboundPacket
