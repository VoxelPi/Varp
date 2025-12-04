package net.voxelpi.varp.mod.fabric.server.command

import com.mojang.brigadier.arguments.ArgumentType
import io.leangen.geantyref.TypeToken
import net.minecraft.command.argument.IdentifierArgumentType
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
import net.voxelpi.varp.mod.fabric.server.FabricVarpServer
import net.voxelpi.varp.mod.fabric.server.command.commands.FabricWarpCommand
import net.voxelpi.varp.mod.server.command.VarpCommandService
import org.incendo.cloud.SenderMapper
import org.incendo.cloud.execution.ExecutionCoordinator
import org.incendo.cloud.fabric.FabricServerCommandManager
import org.incendo.cloud.parser.ArgumentParser

class FabricVarpCommandService : VarpCommandService {

    override val serverProvider: () -> FabricVarpServer = {
        FabricVarpMod.varpServer!!
    }

    override val commandManager: FabricServerCommandManager<FabricVarpCommandSourceStack> = FabricServerCommandManager(
        ExecutionCoordinator.simpleCoordinator(),
        SenderMapper.create(
            { FabricVarpCommandSourceStack(serverProvider(), it) },
            FabricVarpCommandSourceStack::sourceStack,
        )
    )

    init {
        // Use native number argument types.
        commandManager.brigadierManager().setNativeNumberSuggestions(true)

        // Register native mappings.
        registerArgumentMapping<WarpPathParser<FabricVarpCommandSourceStack>>(IdentifierArgumentType.identifier())
        registerArgumentMapping<FolderPathParser<FabricVarpCommandSourceStack>>(IdentifierArgumentType.identifier())
        registerArgumentMapping<NodeParentPathParser<FabricVarpCommandSourceStack>>(IdentifierArgumentType.identifier())
        registerArgumentMapping<NodeChildPathParser<FabricVarpCommandSourceStack>>(IdentifierArgumentType.identifier())
        registerArgumentMapping<NodePathParser<FabricVarpCommandSourceStack>>(IdentifierArgumentType.identifier())
        registerArgumentMapping<WarpParser<FabricVarpCommandSourceStack>>(IdentifierArgumentType.identifier())
        registerArgumentMapping<FolderParser<FabricVarpCommandSourceStack>>(IdentifierArgumentType.identifier())
        registerArgumentMapping<NodeParentParser<FabricVarpCommandSourceStack>>(IdentifierArgumentType.identifier())
        registerArgumentMapping<NodeChildParser<FabricVarpCommandSourceStack>>(IdentifierArgumentType.identifier())
        registerArgumentMapping<NodeParser<FabricVarpCommandSourceStack>>(IdentifierArgumentType.identifier())
        registerArgumentMapping<KeyParser<FabricVarpCommandSourceStack>>(IdentifierArgumentType.identifier())

        // Register common commands.
        registerCommonCommands()
        FabricWarpCommand.register(commandManager, serverProvider)
    }

    private inline fun <reified T : ArgumentParser<FabricVarpCommandSourceStack, *>> registerArgumentMapping(mapping: ArgumentType<*>) {
        commandManager.brigadierManager().registerMapping(object : TypeToken<T>() {}) { builder ->
            builder.to { mapping }
            builder.cloudSuggestions()
        }
    }
}
