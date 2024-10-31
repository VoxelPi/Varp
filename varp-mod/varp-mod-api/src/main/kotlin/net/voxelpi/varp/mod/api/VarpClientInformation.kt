package net.voxelpi.varp.mod.api

/**
 * Stores information about the varp mod installed on a client.
 * @property version The version of the installed mod.
 * @property protocolVersion The protocol version of the installed mod.
 */
@JvmRecord
public data class VarpClientInformation(
    val version: String,
    val protocolVersion: Int,
)
