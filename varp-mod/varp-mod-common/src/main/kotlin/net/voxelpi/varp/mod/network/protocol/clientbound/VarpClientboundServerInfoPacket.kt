package net.voxelpi.varp.mod.network.protocol.clientbound

import net.voxelpi.varp.mod.api.VarpServerInformation
import net.voxelpi.varp.mod.network.protocol.PacketId
import java.util.UUID

/**
 * Send by the server to the client to inform the client about the server mod.
 * @property version The version of the varp mod that is installed on the server.
 * @property protocolVersion The protocol version of the varp mod that is installed on the server.
 * @property identifier The identifier of the varp server mod.
 */
@JvmRecord
@PacketId("server_info")
data class VarpClientboundServerInfoPacket(
    val version: String,
    val protocolVersion: Int,
    val identifier: UUID,
) : VarpClientboundPacket {

    constructor(serverInfo: VarpServerInformation) : this(serverInfo.version, serverInfo.protocolVersion, serverInfo.identifier)

    /**
     * Creates and returns a [VarpServerInformation] instance from the data.
     */
    fun serverInformation(): VarpServerInformation {
        return VarpServerInformation(version, protocolVersion, identifier)
    }
}
