package net.voxelpi.varp.mod.network.protocol

import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation

/**
 * A packet that is used by the varp mod to communicate between the server and the client.
 */
interface VarpPacket {

    companion object {

        /**
         * Returns the packet id from the given packet class.
         */
        fun packetId(packetClass: KClass<out VarpPacket>): String {
            val annotation = packetClass.findAnnotation<PacketId>()
            require(annotation != null) { "The given packet class is missing the PacketId annotation (${packetClass}).)" }
            return annotation.id
        }

        inline fun <reified T : VarpPacket> packetId(): String {
            return packetId(T::class)
        }
    }
}
