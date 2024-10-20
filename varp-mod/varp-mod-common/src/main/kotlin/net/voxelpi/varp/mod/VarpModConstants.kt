package net.voxelpi.varp.mod

import net.kyori.adventure.key.Key

/**
 * Constants that are used by the varp mod.
 */
object VarpModConstants {

    /**
     * The protocol version.
     * Use to determine the compatibility between the client and the server varp mods.
     */
    const val PROTOCOL_VERSION: Int = 0

    /**
     * The id of the plugin channel used by the varp mod for communication between the client and the server varp mods.
     */
    val VARP_PLUGIN_CHANNEL = Key.key("varp", "main")
}
