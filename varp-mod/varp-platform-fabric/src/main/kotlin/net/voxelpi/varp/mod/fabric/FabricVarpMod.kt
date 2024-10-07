package net.voxelpi.varp.mod.fabric

import net.fabricmc.api.ModInitializer
import net.voxelpi.varp.mod.fabric.server.command.FabricVarpCommandService

object FabricVarpMod : ModInitializer {

    lateinit var commandService: FabricVarpCommandService

    override fun onInitialize() {
        commandService = FabricVarpCommandService()
    }
}
