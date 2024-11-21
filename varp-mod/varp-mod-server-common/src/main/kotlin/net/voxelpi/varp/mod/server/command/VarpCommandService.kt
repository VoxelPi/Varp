package net.voxelpi.varp.mod.server.command

import net.voxelpi.varp.mod.server.VarpServerImpl
import net.voxelpi.varp.mod.server.command.commands.ClientCommand
import net.voxelpi.varp.mod.server.command.commands.InfoCommand
import org.incendo.cloud.CommandManager

interface VarpCommandService {

    val commandManager: CommandManager<out VarpCommandSourceStack>

    val serverProvider: () -> VarpServerImpl

    fun registerCommonCommands() {
        ClientCommand.register(commandManager, serverProvider)
        InfoCommand.register(commandManager, serverProvider)
    }
}
