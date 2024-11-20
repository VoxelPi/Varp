package net.voxelpi.varp.mod.server.command

import net.voxelpi.varp.mod.server.VarpServerImpl
import org.incendo.cloud.CommandManager

interface VarpCommandService {

    val commandManager: CommandManager<out VarpCommandSourceStack>

    val serverProvider: () -> VarpServerImpl

    fun registerCommonCommands() {
    }
}
