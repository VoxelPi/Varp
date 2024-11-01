package net.voxelpi.varp.mod.network

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import net.voxelpi.varp.mod.network.protocol.VarpPacket
import net.voxelpi.varp.mod.network.protocol.clientbound.VarpClientboundCreateFolderPacket
import net.voxelpi.varp.mod.network.protocol.clientbound.VarpClientboundCreateWarpPacket
import net.voxelpi.varp.mod.network.protocol.clientbound.VarpClientboundDeleteFolderPacket
import net.voxelpi.varp.mod.network.protocol.clientbound.VarpClientboundDeleteWarpPacket
import net.voxelpi.varp.mod.network.protocol.clientbound.VarpClientboundOpenExplorerPacket
import net.voxelpi.varp.mod.network.protocol.clientbound.VarpClientboundPacket
import net.voxelpi.varp.mod.network.protocol.clientbound.VarpClientboundServerInfoPacket
import net.voxelpi.varp.mod.network.protocol.clientbound.VarpClientboundSyncTreePacket
import net.voxelpi.varp.mod.network.protocol.clientbound.VarpClientboundUpdateFolderPathPacket
import net.voxelpi.varp.mod.network.protocol.clientbound.VarpClientboundUpdateFolderStatePacket
import net.voxelpi.varp.mod.network.protocol.clientbound.VarpClientboundUpdateRootStatePacket
import net.voxelpi.varp.mod.network.protocol.clientbound.VarpClientboundUpdateWarpPathPacket
import net.voxelpi.varp.mod.network.protocol.clientbound.VarpClientboundUpdateWarpStatePacket
import net.voxelpi.varp.mod.network.protocol.serverbound.VarpServerboundClientInfoPacket
import net.voxelpi.varp.mod.network.protocol.serverbound.VarpServerboundCreateFolderPacket
import net.voxelpi.varp.mod.network.protocol.serverbound.VarpServerboundCreateWarpPacket
import net.voxelpi.varp.mod.network.protocol.serverbound.VarpServerboundDeleteFolderPacket
import net.voxelpi.varp.mod.network.protocol.serverbound.VarpServerboundDeleteWarpPacket
import net.voxelpi.varp.mod.network.protocol.serverbound.VarpServerboundModifyFolderPathPacket
import net.voxelpi.varp.mod.network.protocol.serverbound.VarpServerboundModifyFolderStatePacket
import net.voxelpi.varp.mod.network.protocol.serverbound.VarpServerboundModifyRootStatePacket
import net.voxelpi.varp.mod.network.protocol.serverbound.VarpServerboundModifyWarpPathPacket
import net.voxelpi.varp.mod.network.protocol.serverbound.VarpServerboundModifyWarpStatePacket
import net.voxelpi.varp.mod.network.protocol.serverbound.VarpServerboundPacket
import net.voxelpi.varp.mod.network.protocol.serverbound.VarpServerboundTeleportWarpPacket
import net.voxelpi.varp.serializer.gson.varpSerializers
import kotlin.reflect.KClass

/**
 * Manages varp packets.
 */
sealed class VarpPacketRegistry<T : VarpPacket> {

    protected val inboundPacketTypes: MutableMap<String, KClass<out T>> = mutableMapOf()

    /**
     * Registers the given packet in the registry.
     */
    protected inline fun <reified S : T> registerInboundPacket() {
        inboundPacketTypes[VarpPacket.packetId<S>()] = S::class
    }

    /**
     * Serializes the given [packet].
     */
    fun serializePacket(packet: T): Result<String> {
        return runCatching {
            // Get the id from the packet.
            val id = VarpPacket.packetId(packet::class)

            // Build the json packet.
            val payload = serializer.toJsonTree(packet)
            val jsonPacket = JsonObject()
            jsonPacket.addProperty("id", id)
            jsonPacket.add("data", payload)
            jsonPacket.toString()
        }
    }

    /**
     * Deserializes the given [serializedPacket]
     */
    fun deserializePacket(serializedPacket: String): Result<T> {
        return runCatching {
            // Check that the json packet is a json object.
            val jsonPacket = JsonParser.parseString(serializedPacket)
            check(jsonPacket is JsonObject) { "Varp packet must be a json object. \"$serializedPacket\"" }

            // Get the packet type of the json packet.
            val id = jsonPacket["id"].asString
            check(id != null) { "Varp packet has no id. \"$serializedPacket\"" }
            val packetType = inboundPacketTypes[id]
            check(packetType != null) { "Unknown varp packet $id. \"$serializedPacket\"" }

            // Deserialize the packet.
            serializer.fromJson(jsonPacket.get("data"), packetType.java)
        }
    }

    /**
     * Registry for all clientbound packets, that are packets that are send from the server to the client.
     */
    object Clientbound : VarpPacketRegistry<VarpClientboundPacket>() {
        init {
            registerInboundPacket<VarpClientboundCreateFolderPacket>()
            registerInboundPacket<VarpClientboundCreateWarpPacket>()
            registerInboundPacket<VarpClientboundDeleteFolderPacket>()
            registerInboundPacket<VarpClientboundDeleteWarpPacket>()
            registerInboundPacket<VarpClientboundOpenExplorerPacket>()
            registerInboundPacket<VarpClientboundServerInfoPacket>()
            registerInboundPacket<VarpClientboundSyncTreePacket>()
            registerInboundPacket<VarpClientboundUpdateFolderPathPacket>()
            registerInboundPacket<VarpClientboundUpdateFolderStatePacket>()
            registerInboundPacket<VarpClientboundUpdateRootStatePacket>()
            registerInboundPacket<VarpClientboundUpdateWarpPathPacket>()
            registerInboundPacket<VarpClientboundUpdateWarpStatePacket>()
        }
    }

    /**
     * Registry for all serverbound packets, that are packets that are send from the client to the server.
     */
    object Serverbound : VarpPacketRegistry<VarpServerboundPacket>() {
        init {
            registerInboundPacket<VarpServerboundClientInfoPacket>()
            registerInboundPacket<VarpServerboundCreateFolderPacket>()
            registerInboundPacket<VarpServerboundCreateWarpPacket>()
            registerInboundPacket<VarpServerboundDeleteFolderPacket>()
            registerInboundPacket<VarpServerboundDeleteWarpPacket>()
            registerInboundPacket<VarpServerboundModifyFolderPathPacket>()
            registerInboundPacket<VarpServerboundModifyFolderStatePacket>()
            registerInboundPacket<VarpServerboundModifyRootStatePacket>()
            registerInboundPacket<VarpServerboundModifyWarpPathPacket>()
            registerInboundPacket<VarpServerboundModifyWarpStatePacket>()
            registerInboundPacket<VarpServerboundTeleportWarpPacket>()
        }
    }

    companion object {

        /**
         * The serializer used for packet serialization.
         */
        private val serializer: Gson = GsonBuilder().apply {
            GsonComponentSerializer.gson().populator().apply(this)
            varpSerializers()
        }.create()
    }
}
