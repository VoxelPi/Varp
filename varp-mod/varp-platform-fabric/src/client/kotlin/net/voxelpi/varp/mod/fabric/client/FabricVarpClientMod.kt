package net.voxelpi.varp.mod.fabric.client

import net.fabricmc.api.ClientModInitializer

object FabricVarpClientMod : ClientModInitializer {

    lateinit var client: FabricVarpClient
        private set

    override fun onInitializeClient() {
        client = FabricVarpClient()
    }
}
