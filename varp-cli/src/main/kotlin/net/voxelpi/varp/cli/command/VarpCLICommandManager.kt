package net.voxelpi.varp.cli.command

import io.leangen.geantyref.TypeToken
import net.kyori.adventure.key.Key
import net.voxelpi.event.post
import net.voxelpi.varp.cli.VarpCLI
import net.voxelpi.varp.cli.command.commands.ClearCommand
import net.voxelpi.varp.cli.command.commands.CopyCommand
import net.voxelpi.varp.cli.command.commands.CreateCommand
import net.voxelpi.varp.cli.command.commands.ListCommand
import net.voxelpi.varp.cli.command.commands.MountsCommand
import net.voxelpi.varp.cli.command.commands.MoveCommand
import net.voxelpi.varp.cli.command.commands.PropertiesCommand
import net.voxelpi.varp.cli.command.commands.RemoveCommand
import net.voxelpi.varp.cli.command.commands.RepositoriesCommand
import net.voxelpi.varp.cli.command.commands.StopCommand
import net.voxelpi.varp.cli.command.commands.TagsCommand
import net.voxelpi.varp.extras.cloud.parser.KeyParser
import net.voxelpi.varp.extras.cloud.parser.path.FolderPathParser
import net.voxelpi.varp.extras.cloud.parser.path.NodeParentPathParser
import net.voxelpi.varp.extras.cloud.parser.path.WarpPathParser
import net.voxelpi.varp.extras.cloud.parser.tree.FolderParser
import net.voxelpi.varp.extras.cloud.parser.tree.NodeParentParser
import net.voxelpi.varp.extras.cloud.parser.tree.WarpParser
import net.voxelpi.varp.warp.Folder
import net.voxelpi.varp.warp.NodeParent
import net.voxelpi.varp.warp.Warp
import net.voxelpi.varp.warp.path.FolderPath
import net.voxelpi.varp.warp.path.NodeParentPath
import net.voxelpi.varp.warp.path.WarpPath
import org.incendo.cloud.CommandManager
import org.incendo.cloud.execution.ExecutionCoordinator
import org.incendo.cloud.internal.CommandRegistrationHandler
import kotlin.reflect.full.createInstance

class VarpCLICommandManager(
    private val cli: VarpCLI,
) : CommandManager<VarpCLICommandSender>(ExecutionCoordinator.simpleCoordinator(), CommandRegistrationHandler.nullCommandRegistrationHandler()) {

    private val eventScope = cli.eventScope.createSubScope()

    init {
        // Register parsers
        registerParsers()

        // Register all internal commands.
        registerCommands()

        // Post registration event.
        eventScope.post(CommandsRegistrationEvent(cli, this))
    }

    override fun hasPermission(sender: VarpCLICommandSender, permission: String): Boolean {
        return true
    }

    private fun registerParsers() {
        // Path parsers.
        parserRegistry().registerParserSupplier(TypeToken.get(NodeParentPath::class.java)) { NodeParentPathParser { cli.tree } }
        parserRegistry().registerParserSupplier(TypeToken.get(FolderPath::class.java)) { FolderPathParser { cli.tree } }
        parserRegistry().registerParserSupplier(TypeToken.get(WarpPath::class.java)) { WarpPathParser { cli.tree } }

        // Node parsers.
        parserRegistry().registerParserSupplier(TypeToken.get(NodeParent::class.java)) { NodeParentParser { cli.tree } }
        parserRegistry().registerParserSupplier(TypeToken.get(Folder::class.java)) { FolderParser { cli.tree } }
        parserRegistry().registerParserSupplier(TypeToken.get(Warp::class.java)) { WarpParser { cli.tree } }

        // Other parsers
        parserRegistry().registerParserSupplier(TypeToken.get(Key::class.java)) { KeyParser() }
    }

    private fun registerCommands() {
        registerCommand(ClearCommand)
        registerCommand(CopyCommand)
        registerCommand(CreateCommand)
        registerCommand(ListCommand)
        registerCommand(MountsCommand)
        registerCommand(MoveCommand)
        registerCommand(RemoveCommand)
        registerCommand(RepositoriesCommand)
        registerCommand(StopCommand)
        registerCommand(TagsCommand)
        registerCommand(PropertiesCommand)
    }

    private fun registerCommand(instance: Any) {
        eventScope.registerAnnotated(instance)
    }

    private inline fun <reified C : Any> registerCommand() {
        val instance = C::class.createInstance()
        eventScope.registerAnnotated(instance)
    }
}
