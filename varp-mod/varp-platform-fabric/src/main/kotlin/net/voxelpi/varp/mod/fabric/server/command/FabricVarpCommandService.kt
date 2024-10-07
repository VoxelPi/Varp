package net.voxelpi.varp.mod.fabric.server.command

import net.voxelpi.varp.mod.server.command.VarpCommandService
import org.incendo.cloud.SenderMapper
import org.incendo.cloud.execution.ExecutionCoordinator
import org.incendo.cloud.fabric.FabricServerCommandManager

class FabricVarpCommandService : VarpCommandService {

    override val commandManager: FabricServerCommandManager<FabricVarpCommandSourceStack> = FabricServerCommandManager(
        ExecutionCoordinator.simpleCoordinator(),
        SenderMapper.create(::FabricVarpCommandSourceStack, FabricVarpCommandSourceStack::sourceStack)
    )

    init {
        // Use native number argument types.
        commandManager.brigadierManager().setNativeNumberSuggestions(true)
    }
}
