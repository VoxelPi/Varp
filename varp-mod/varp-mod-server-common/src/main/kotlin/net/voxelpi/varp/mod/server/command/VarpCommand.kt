package net.voxelpi.varp.mod.server.command

import net.voxelpi.varp.mod.server.VarpServerImpl
import org.incendo.cloud.CommandManager

interface VarpCommand {

    fun register(manager: CommandManager<out VarpCommandSourceStack>, serverProvider: () -> VarpServerImpl)
}
