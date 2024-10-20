package net.voxelpi.varp.mod.fabric.client.network

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.voxelpi.varp.mod.VarpModConstants
import net.voxelpi.varp.mod.client.network.VarpClientNetworkHandler
import net.voxelpi.varp.mod.fabric.client.FabricVarpClient
import net.voxelpi.varp.mod.fabric.network.FabricVarpPacket
import net.voxelpi.varp.mod.fabric.util.toIdentifier

class FabricVarpClientNetworkHandler(
    override val client: FabricVarpClient,
) : VarpClientNetworkHandler(client) {

    init {
        PayloadTypeRegistry.playC2S().register(FabricVarpPacket.PACKET_ID, FabricVarpPacket.PACKET_CODEC)
        PayloadTypeRegistry.playS2C().register(FabricVarpPacket.PACKET_ID, FabricVarpPacket.PACKET_CODEC)
        ClientPlayNetworking.registerGlobalReceiver(FabricVarpPacket.PACKET_ID, this::onPluginMessageReceived)
        client.logger.info("Registered varp channel ${VarpModConstants.VARP_PLUGIN_CHANNEL.toIdentifier()}")
    }

    override fun sendServerboundPluginMessage(message: String) {
        ClientPlayNetworking.send(FabricVarpPacket(message))
    }

    private fun onPluginMessageReceived(packet: FabricVarpPacket, context: ClientPlayNetworking.Context) {
        val message = packet.payload
        context.client().execute {
            handleClientboundPluginMessage(message)
        }
    }
}
