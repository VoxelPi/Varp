package net.voxelpi.varp.mod.network.protocol.serverbound

import net.voxelpi.varp.mod.network.protocol.PacketId

/**
 * Send by the client to the server to initialize the client-server bridge.
 * @property version The version of the varp mod that is installed on the client.
 * @property protocolVersion The protocol version of the varp mod that is installed on the client.
 */
@JvmRecord
@PacketId("initialize")
data class VarpServerboundInitializationPacket(
    val version: String,
    val protocolVersion: Int,
) : VarpServerboundPacket
