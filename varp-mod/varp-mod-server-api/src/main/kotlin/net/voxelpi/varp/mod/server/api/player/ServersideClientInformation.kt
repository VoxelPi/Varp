package net.voxelpi.varp.mod.server.api.player

/**
 * Stores information about the client mod installed on a client.
 * @property version The version of the installed mod.
 * @property protocolVersion The protocol version of the installed mod.
 */
@JvmRecord
public data class ServersideClientInformation(
    val version: String,
    val protocolVersion: Int,
)
