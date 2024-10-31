package net.voxelpi.varp.mod.api

/**
 * Stores information about the varp server mod installed on a server.
 * @property version The version of the installed mod.
 * @property protocolVersion The protocol version of the installed mod.
 */
@JvmRecord
public data class VarpServerInformation(
    val version: String,
    val protocolVersion: Int,
)
