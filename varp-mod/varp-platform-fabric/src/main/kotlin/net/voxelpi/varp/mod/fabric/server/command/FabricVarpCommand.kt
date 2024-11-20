package net.voxelpi.varp.mod.fabric.server.command

import net.voxelpi.varp.mod.fabric.server.FabricVarpServer
import org.incendo.cloud.fabric.FabricServerCommandManager

interface FabricVarpCommand {

    fun register(manager: FabricServerCommandManager<FabricVarpCommandSourceStack>, serverProvider: () -> FabricVarpServer)
}
