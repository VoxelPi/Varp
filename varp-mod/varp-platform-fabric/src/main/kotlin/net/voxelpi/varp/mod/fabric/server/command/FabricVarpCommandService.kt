package net.voxelpi.varp.mod.fabric.server.command

import com.mojang.brigadier.arguments.ArgumentType
import io.leangen.geantyref.TypeToken
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
import net.voxelpi.varp.mod.fabric.FabricVarpMod
import net.voxelpi.varp.mod.fabric.server.command.commands.FabricWarpCommand
import net.voxelpi.varp.mod.server.command.VarpCommandService
import net.voxelpi.varp.mod.server.command.VarpModCommandArguments
import org.incendo.cloud.SenderMapper
import org.incendo.cloud.execution.ExecutionCoordinator
import org.incendo.cloud.fabric.FabricServerCommandManager
import org.incendo.cloud.parser.ArgumentParser

class FabricVarpCommandService : VarpCommandService {

    override val commandManager: FabricServerCommandManager<FabricVarpCommandSourceStack> = FabricServerCommandManager(
        ExecutionCoordinator.simpleCoordinator(),
        SenderMapper.create(
            { FabricVarpCommandSourceStack(FabricVarpMod.varpServer!!, it) },
            FabricVarpCommandSourceStack::sourceStack,
        )
    )

    init {
        // Use native number argument types.
        commandManager.brigadierManager().setNativeNumberSuggestions(true)

        // Register native mappings.
        registerArgumentMapping<WarpPathParser<FabricVarpCommandSourceStack>>(IdentifierArgument.id())
        registerArgumentMapping<FolderPathParser<FabricVarpCommandSourceStack>>(IdentifierArgument.id())
        registerArgumentMapping<NodeParentPathParser<FabricVarpCommandSourceStack>>(IdentifierArgument.id())
        registerArgumentMapping<NodeChildPathParser<FabricVarpCommandSourceStack>>(IdentifierArgument.id())
        registerArgumentMapping<NodePathParser<FabricVarpCommandSourceStack>>(IdentifierArgument.id())
        registerArgumentMapping<WarpParser<FabricVarpCommandSourceStack>>(IdentifierArgument.id())
        registerArgumentMapping<FolderParser<FabricVarpCommandSourceStack>>(IdentifierArgument.id())
        registerArgumentMapping<NodeParentParser<FabricVarpCommandSourceStack>>(IdentifierArgument.id())
        registerArgumentMapping<NodeChildParser<FabricVarpCommandSourceStack>>(IdentifierArgument.id())
        registerArgumentMapping<NodeParser<FabricVarpCommandSourceStack>>(IdentifierArgument.id())
        registerArgumentMapping<KeyParser<FabricVarpCommandSourceStack>>(IdentifierArgument.id())

        // Register preprocessor for shared arguments.
        commandManager.registerCommandPreProcessor { context ->
            context.commandContext().apply {
                FabricVarpMod.varpServer?.let { server ->
                    store(VarpCommandArguments.TREE, server.tree)
                    store(VarpCommandArguments.COMPOSITOR, server.compositor)
                    store(VarpCommandArguments.ENVIRONMENT, server.environment)
                    store(VarpModCommandArguments.MESSAGE_SERVICE, server.messages)
                    store(VarpModCommandArguments.COROUTINE_SCOPE, server.coroutineScope)
                    store(FabricVarpCommandArguments.SERVER, server)
                }
            }
        }

        // Register common commands.
        registerCommonCommands()
        FabricWarpCommand.register(commandManager)
    }

    private inline fun <reified T : ArgumentParser<FabricVarpCommandSourceStack, *>> registerArgumentMapping(mapping: ArgumentType<*>) {
        commandManager.brigadierManager().registerMapping(object : TypeToken<T>() {}) { builder ->
            builder.to { mapping }
            builder.cloudSuggestions()
        }
    }
}
