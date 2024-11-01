package net.voxelpi.varp.mod.fabric.client

import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.voxelpi.varp.mod.network.protocol.serverbound.VarpServerboundClientInfoPacket

object FabricVarpClientMod : ClientModInitializer {

    lateinit var client: FabricVarpClient
        private set

    override fun onInitializeClient() {
        client = FabricVarpClient()

        ClientLifecycleEvents.CLIENT_STOPPING.register { _ ->
            client.cleanup()
        }

        ClientPlayConnectionEvents.JOIN.register { networkHandler, packetSender, _ ->
            client.clientNetworkHandler.sendServerboundPacket(VarpServerboundClientInfoPacket(client.info))
        }
    }
}
