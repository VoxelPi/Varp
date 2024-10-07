package net.voxelpi.varp.mod.paper.command

import io.papermc.paper.plugin.bootstrap.BootstrapContext
import net.voxelpi.varp.mod.server.command.VarpCommandService
import org.incendo.cloud.SenderMapper
import org.incendo.cloud.execution.ExecutionCoordinator
import org.incendo.cloud.paper.PaperCommandManager

@Suppress("UnstableApiUsage")
class PaperVarpCommandService(
    bootstrapContext: BootstrapContext,
) : VarpCommandService {

    override val commandManager: PaperCommandManager<PaperVarpCommandSourceStack> = PaperCommandManager.builder(
        SenderMapper.create(::PaperVarpCommandSourceStack, PaperVarpCommandSourceStack::sourceStack)
    )
        .executionCoordinator(ExecutionCoordinator.simpleCoordinator())
        .buildBootstrapped(bootstrapContext)

    init {
        // Use native number argument types.
        commandManager.brigadierManager().setNativeNumberSuggestions(true)
    }
}
