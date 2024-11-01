package net.voxelpi.varp.mod.network.protocol.serverbound

import net.voxelpi.varp.mod.api.VarpClientInformation
import net.voxelpi.varp.mod.network.protocol.PacketId

/**
 * Send by the client to the server to inform the server about the client mod.
 * @property version The version of the varp mod that is installed on the client.
 * @property protocolVersion The protocol version of the varp mod that is installed on the client.
 */
@JvmRecord
@PacketId("client_info")
data class VarpServerboundClientInfoPacket(
    val version: String,
    val protocolVersion: Int,
) : VarpServerboundPacket {

    constructor(clientInfo: VarpClientInformation) : this(clientInfo.version, clientInfo.protocolVersion)

    /**
     * Creates and returns a [VarpClientInformation] instance from the data.
     */
    fun clientInformation(): VarpClientInformation {
        return VarpClientInformation(version, protocolVersion)
    }
}
