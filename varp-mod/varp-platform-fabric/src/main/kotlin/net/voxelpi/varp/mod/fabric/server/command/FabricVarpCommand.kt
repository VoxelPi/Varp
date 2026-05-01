package net.voxelpi.varp.mod.fabric.server.command

import org.incendo.cloud.fabric.FabricServerCommandManager

interface FabricVarpCommand {

    fun register(manager: FabricServerCommandManager<FabricVarpCommandSourceStack>)
}
