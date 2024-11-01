package net.voxelpi.varp.mod.api

import java.util.UUID

/**
 * Stores information about the varp server mod installed on a server.
 * @property version The version of the installed mod.
 * @property protocolVersion The protocol version of the installed mod.
 * @property identifier A unique id to identify the server mod instance.
 */
@JvmRecord
public data class VarpServerInformation(
    val version: String,
    val protocolVersion: Int,
    val identifier: UUID,
)
