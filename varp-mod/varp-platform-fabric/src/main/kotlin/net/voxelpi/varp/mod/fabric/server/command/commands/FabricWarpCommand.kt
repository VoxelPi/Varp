package net.voxelpi.varp.mod.fabric.server.command.commands

import net.voxelpi.varp.extras.cloud.parser.tree.warpParser
import net.voxelpi.varp.mod.fabric.server.command.FabricVarpCommand
import net.voxelpi.varp.mod.fabric.server.command.FabricVarpCommandArguments
import net.voxelpi.varp.mod.fabric.server.command.FabricVarpCommandSourceStack
import net.voxelpi.varp.tree.Warp
import org.incendo.cloud.fabric.FabricServerCommandManager
import org.incendo.cloud.kotlin.extension.buildAndRegister
import org.incendo.cloud.minecraft.modded.data.MultipleEntitySelector
import org.incendo.cloud.minecraft.modded.parser.VanillaArgumentParsers.multipleEntitySelectorParser

object FabricWarpCommand : FabricVarpCommand {

    override fun register(manager: FabricServerCommandManager<FabricVarpCommandSourceStack>) {
        manager.buildAndRegister("warp") {
            permission("varp.others")

            required("warp", warpParser())
            required("targets", multipleEntitySelectorParser())

            handler { context ->
                val server = context[FabricVarpCommandArguments.SERVER]
                val warp: Warp = context["warp"]
                val targetsSelector: MultipleEntitySelector = context["targets"]
                val targets = targetsSelector.values().map(server.entityService::entity)

                context.sender().teleportToWarp(warp, targets)
            }
        }
    }
}
