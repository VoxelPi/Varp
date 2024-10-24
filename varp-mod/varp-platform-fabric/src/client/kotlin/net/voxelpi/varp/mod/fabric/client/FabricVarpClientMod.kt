package net.voxelpi.varp.mod.fabric.client

import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents

object FabricVarpClientMod : ClientModInitializer {

    lateinit var client: FabricVarpClient
        private set

    override fun onInitializeClient() {
        client = FabricVarpClient()

        ClientLifecycleEvents.CLIENT_STOPPING.register { _ ->
            client.cleanup()
        }
    }
}
