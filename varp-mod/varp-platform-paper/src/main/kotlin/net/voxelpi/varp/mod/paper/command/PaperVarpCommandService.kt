package net.voxelpi.varp.mod.paper.command

import com.mojang.brigadier.arguments.ArgumentType
import io.leangen.geantyref.TypeToken
import io.papermc.paper.plugin.bootstrap.BootstrapContext
import net.minecraft.commands.arguments.ResourceLocationArgument
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
import org.incendo.cloud.SenderMapper
import org.incendo.cloud.execution.ExecutionCoordinator
import org.incendo.cloud.paper.PaperCommandManager
import org.incendo.cloud.parser.ArgumentParser

@Suppress("UnstableApiUsage")
class PaperVarpCommandService(
    bootstrapContext: BootstrapContext,
) : VarpCommandService {

    private lateinit var server: PaperVarpServer

    override val serverProvider: () -> PaperVarpServer = {
        server
    }

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
        registerArgumentMapping<WarpPathParser<PaperVarpCommandSourceStack>>(ResourceLocationArgument.id())
        registerArgumentMapping<FolderPathParser<PaperVarpCommandSourceStack>>(ResourceLocationArgument.id())
        registerArgumentMapping<NodeParentPathParser<PaperVarpCommandSourceStack>>(ResourceLocationArgument.id())
        registerArgumentMapping<NodeChildPathParser<PaperVarpCommandSourceStack>>(ResourceLocationArgument.id())
        registerArgumentMapping<NodePathParser<PaperVarpCommandSourceStack>>(ResourceLocationArgument.id())
        registerArgumentMapping<WarpParser<PaperVarpCommandSourceStack>>(ResourceLocationArgument.id())
        registerArgumentMapping<FolderParser<PaperVarpCommandSourceStack>>(ResourceLocationArgument.id())
        registerArgumentMapping<NodeParentParser<PaperVarpCommandSourceStack>>(ResourceLocationArgument.id())
        registerArgumentMapping<NodeChildParser<PaperVarpCommandSourceStack>>(ResourceLocationArgument.id())
        registerArgumentMapping<NodeParser<PaperVarpCommandSourceStack>>(ResourceLocationArgument.id())
        registerArgumentMapping<KeyParser<PaperVarpCommandSourceStack>>(ResourceLocationArgument.id())

        // Register common commands
        registerCommonCommands()
        PaperWarpCommand.register(commandManager, serverProvider)
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
