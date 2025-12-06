package net.voxelpi.varp.mod.server.command

import net.voxelpi.varp.mod.server.VarpServerImpl
import net.voxelpi.varp.mod.server.command.commands.ClientCommand
import net.voxelpi.varp.mod.server.command.commands.CreateCommand
import net.voxelpi.varp.mod.server.command.commands.DeleteCommand
import net.voxelpi.varp.mod.server.command.commands.InfoCommand
import net.voxelpi.varp.mod.server.command.commands.ListCommand
import net.voxelpi.varp.mod.server.command.commands.MountsCommand
import net.voxelpi.varp.mod.server.command.commands.MoveCommand
import net.voxelpi.varp.mod.server.command.commands.ReloadCommand
import net.voxelpi.varp.mod.server.command.commands.RepositoriesCommand
import net.voxelpi.varp.mod.server.command.commands.TeleportationLogCommand
import net.voxelpi.varp.mod.server.command.commands.WarpCommand
import org.incendo.cloud.CommandManager

interface VarpCommandService {

    val commandManager: CommandManager<out VarpCommandSourceStack>

    val serverProvider: () -> VarpServerImpl

    fun registerCommonCommands() {
        ClientCommand.register(commandManager, serverProvider)
        CreateCommand.register(commandManager, serverProvider)
        DeleteCommand.register(commandManager, serverProvider)
        InfoCommand.register(commandManager, serverProvider)
        ListCommand.register(commandManager, serverProvider)
        MountsCommand.register(commandManager, serverProvider)
        MoveCommand.register(commandManager, serverProvider)
        ReloadCommand.register(commandManager, serverProvider)
        RepositoriesCommand.register(commandManager, serverProvider)
        TeleportationLogCommand.register(commandManager, serverProvider)
        WarpCommand.register(commandManager, serverProvider)
    }
}
