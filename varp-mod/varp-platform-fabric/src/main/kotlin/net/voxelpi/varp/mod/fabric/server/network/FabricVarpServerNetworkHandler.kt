package net.voxelpi.varp.mod.fabric.server.network

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.voxelpi.varp.mod.VarpModConstants
import net.voxelpi.varp.mod.fabric.network.FabricVarpPacket
import net.voxelpi.varp.mod.fabric.server.FabricVarpServer
import net.voxelpi.varp.mod.fabric.server.player.FabricVarpServerPlayer
import net.voxelpi.varp.mod.fabric.util.toIdentifier
import net.voxelpi.varp.mod.server.network.VarpServerNetworkHandler
import net.voxelpi.varp.mod.server.player.VarpServerPlayerImpl

class FabricVarpServerNetworkHandler(
    override val server: FabricVarpServer,
) : VarpServerNetworkHandler(server) {

    init {
        if (server.server.isDedicated) {
            PayloadTypeRegistry.playC2S().register(FabricVarpPacket.PACKET_ID, FabricVarpPacket.PACKET_CODEC)
            PayloadTypeRegistry.playS2C().register(FabricVarpPacket.PACKET_ID, FabricVarpPacket.PACKET_CODEC)
        }
        ServerPlayNetworking.registerGlobalReceiver(FabricVarpPacket.PACKET_ID, this::onPluginMessageReceived)
        server.logger.info("Registered varp channel ${VarpModConstants.VARP_PLUGIN_CHANNEL.toIdentifier()}")
    }

    fun cleanup() {
        ServerPlayNetworking.unregisterGlobalReceiver(VarpModConstants.VARP_PLUGIN_CHANNEL.toIdentifier())
    }

    override fun sendClientboundPluginMessage(message: String, player: VarpServerPlayerImpl) {
        ServerPlayNetworking.send((player as FabricVarpServerPlayer).player, FabricVarpPacket(message))
    }

    private fun onPluginMessageReceived(packet: FabricVarpPacket, context: ServerPlayNetworking.Context) {
        val message = packet.payload
        val player = server.playerService.player(context.player())
        handleServerboundPluginMessage(player, message)
    }
}
