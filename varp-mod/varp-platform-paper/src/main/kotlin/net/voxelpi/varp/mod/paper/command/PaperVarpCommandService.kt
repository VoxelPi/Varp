package net.voxelpi.varp.mod.paper.command

import com.mojang.brigadier.arguments.ArgumentType
import io.leangen.geantyref.TypeToken
import io.papermc.paper.plugin.bootstrap.BootstrapContext
import net.minecraft.commands.arguments.IdentifierArgument
import net.voxelpi.varp.extras.cloud.VarpCommandArguments
import net.voxelpi.varp.extras.cloud.parser.KeyParser
import net.voxelpi.varp.extras.cloud.parser.path.FolderPathParser
import net.voxelpi.varp.extras.cloud.parser.path.NodeChildPathParser
import net.voxelpi.varp.extras.cloud.parser.path.NodeParentPathParser
import net.voxelpi.varp.extras.cloud.parser.path.NodePathParser
import net.voxelpi.varp.extras.cloud.parser.path.WarpPathParser
import net.voxelpi.varp.extras.cloud.parser.tree.FolderParser
import net.voxelpi.varp.extras.cloud.parser.tree.NodeChildParser
import net.voxelpi.varp.extras.cloud.parser.tree.NodeParentParser
import net.voxelpi.varp.extras.cloud.parser.tree.NodeParser
import net.voxelpi.varp.extras.cloud.parser.tree.WarpParser
import net.voxelpi.varp.mod.paper.PaperVarpServer
import net.voxelpi.varp.mod.paper.command.commands.PaperWarpCommand
import net.voxelpi.varp.mod.server.command.VarpCommandService
import net.voxelpi.varp.mod.server.command.VarpModCommandArguments
import org.incendo.cloud.SenderMapper
import org.incendo.cloud.execution.ExecutionCoordinator
import org.incendo.cloud.paper.PaperCommandManager
import org.incendo.cloud.parser.ArgumentParser

@Suppress("UnstableApiUsage")
class PaperVarpCommandService(
    bootstrapContext: BootstrapContext,
) : VarpCommandService {

    private lateinit var server: PaperVarpServer

    override val commandManager: PaperCommandManager<PaperVarpCommandSourceStack> = PaperCommandManager.builder(
        SenderMapper.create(
            { PaperVarpCommandSourceStack(server, it) },
            PaperVarpCommandSourceStack::sourceStack,
        )
    )
        .executionCoordinator(ExecutionCoordinator.simpleCoordinator())
        .buildBootstrapped(bootstrapContext)

    init {
        // Use native number argument types.
        commandManager.brigadierManager().setNativeNumberSuggestions(true)

        // Register native mappings.
        registerArgumentMapping<WarpPathParser<PaperVarpCommandSourceStack>>(IdentifierArgument.id())
        registerArgumentMapping<FolderPathParser<PaperVarpCommandSourceStack>>(IdentifierArgument.id())
        registerArgumentMapping<NodeParentPathParser<PaperVarpCommandSourceStack>>(IdentifierArgument.id())
        registerArgumentMapping<NodeChildPathParser<PaperVarpCommandSourceStack>>(IdentifierArgument.id())
        registerArgumentMapping<NodePathParser<PaperVarpCommandSourceStack>>(IdentifierArgument.id())
        registerArgumentMapping<WarpParser<PaperVarpCommandSourceStack>>(IdentifierArgument.id())
        registerArgumentMapping<FolderParser<PaperVarpCommandSourceStack>>(IdentifierArgument.id())
        registerArgumentMapping<NodeParentParser<PaperVarpCommandSourceStack>>(IdentifierArgument.id())
        registerArgumentMapping<NodeChildParser<PaperVarpCommandSourceStack>>(IdentifierArgument.id())
        registerArgumentMapping<NodeParser<PaperVarpCommandSourceStack>>(IdentifierArgument.id())
        registerArgumentMapping<KeyParser<PaperVarpCommandSourceStack>>(IdentifierArgument.id())

        // Register preprocessor for shared arguments.
        commandManager.registerCommandPreProcessor { context ->
            context.commandContext().apply {
                store(VarpCommandArguments.TREE, server.tree)
                store(VarpCommandArguments.COMPOSITOR, server.compositor)
                store(VarpCommandArguments.ENVIRONMENT, server.environment)
                store(VarpModCommandArguments.MESSAGE_SERVICE, server.messages)
                store(VarpModCommandArguments.COROUTINE_SCOPE, server.coroutineScope)
                store(PaperVarpCommandArguments.SERVER, server)
            }
        }

        // Register common commands
        registerCommonCommands()
        PaperWarpCommand.register(commandManager)
    }

    internal fun registerServer(server: PaperVarpServer) {
        this.server = server
    }

    private inline fun <reified T : ArgumentParser<PaperVarpCommandSourceStack, *>> registerArgumentMapping(mapping: ArgumentType<*>) {
        commandManager.brigadierManager().registerMapping(object : TypeToken<T>() {}) { builder ->
            builder.to { mapping }
            builder.cloudSuggestions()
        }
    }
}
