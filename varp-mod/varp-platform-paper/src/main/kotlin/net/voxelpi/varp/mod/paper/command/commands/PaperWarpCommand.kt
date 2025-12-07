package net.voxelpi.varp.mod.paper.command.commands

import net.voxelpi.varp.extras.cloud.parser.tree.warpParser
import net.voxelpi.varp.mod.paper.PaperVarpServer
import net.voxelpi.varp.mod.paper.command.PaperVarpCommand
import net.voxelpi.varp.mod.paper.command.PaperVarpCommandSourceStack
import net.voxelpi.varp.tree.Warp
import org.incendo.cloud.bukkit.data.MultipleEntitySelector
import org.incendo.cloud.bukkit.parser.selector.MultipleEntitySelectorParser.multipleEntitySelectorParser
import org.incendo.cloud.kotlin.extension.buildAndRegister
import org.incendo.cloud.paper.PaperCommandManager

object PaperWarpCommand : PaperVarpCommand {

    override fun register(manager: PaperCommandManager<PaperVarpCommandSourceStack>, serverProvider: () -> PaperVarpServer) {
        manager.buildAndRegister("warp") {
            permission("varp.others")

            required("warp", warpParser { serverProvider().tree })
            required("targets", multipleEntitySelectorParser())

            handler { context ->
                val server = serverProvider()
                val warp: Warp = context["warp"]
                val targetsSelector: MultipleEntitySelector = context["targets"]
                val targets = targetsSelector.values().map(server.entityService::entity)

                context.sender().teleportToWarp(warp, targets)
            }
        }
    }
}
