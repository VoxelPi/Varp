package net.voxelpi.varp.mod.network.protocol.clientbound

import net.voxelpi.varp.mod.network.protocol.PacketId
import net.voxelpi.varp.warp.path.NodeParentPath

/**
 * Send by the server to the client, to signal the client that it should open the explorer gui.
 * @property path The path at which the explorer should be opened.
 */
@JvmRecord
@PacketId("open_explorer")
data class VarpClientboundOpenExplorerPacket(
    val path: NodeParentPath,
) : VarpClientboundPacket
