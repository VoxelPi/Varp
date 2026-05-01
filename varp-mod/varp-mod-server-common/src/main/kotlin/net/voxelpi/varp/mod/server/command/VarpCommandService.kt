package net.voxelpi.varp.mod.server.command

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

    fun registerCommonCommands() {
        ClientCommand.register(commandManager)
        CreateCommand.register(commandManager)
        DeleteCommand.register(commandManager)
        InfoCommand.register(commandManager)
        ListCommand.register(commandManager)
        MountsCommand.register(commandManager)
        MoveCommand.register(commandManager)
        ReloadCommand.register(commandManager)
        RepositoriesCommand.register(commandManager)
        TeleportationLogCommand.register(commandManager)
        WarpCommand.register(commandManager)
    }
}
