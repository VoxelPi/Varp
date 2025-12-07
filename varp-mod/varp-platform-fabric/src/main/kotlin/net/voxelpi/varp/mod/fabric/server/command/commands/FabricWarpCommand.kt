package net.voxelpi.varp.mod.fabric.server.command.commands

import net.voxelpi.varp.extras.cloud.parser.tree.warpParser
import net.voxelpi.varp.mod.fabric.server.FabricVarpServer
import net.voxelpi.varp.mod.fabric.server.command.FabricVarpCommand
import net.voxelpi.varp.mod.fabric.server.command.FabricVarpCommandSourceStack
import net.voxelpi.varp.tree.Warp
import org.incendo.cloud.fabric.FabricServerCommandManager
import org.incendo.cloud.kotlin.extension.buildAndRegister
import org.incendo.cloud.minecraft.modded.data.MultipleEntitySelector
import org.incendo.cloud.minecraft.modded.parser.VanillaArgumentParsers.multipleEntitySelectorParser

object FabricWarpCommand : FabricVarpCommand {

    override fun register(manager: FabricServerCommandManager<FabricVarpCommandSourceStack>, serverProvider: () -> FabricVarpServer) {
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
