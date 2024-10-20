package net.voxelpi.varp.mod.network.protocol.serverbound

import net.voxelpi.varp.mod.network.protocol.VarpPacket

/**
 * A packet that is used by the varp mod to send data from the server to the client.
 */
sealed interface VarpServerboundPacket : VarpPacket
