package net.voxelpi.varp.mod.server.command.commands

import net.voxelpi.varp.extras.cloud.parser.tree.warpParser
import net.voxelpi.varp.mod.server.VarpServerImpl
import net.voxelpi.varp.mod.server.command.VarpCommand
import net.voxelpi.varp.mod.server.command.VarpCommandSourceStack
import net.voxelpi.varp.tree.Warp
import org.incendo.cloud.CommandManager
import org.incendo.cloud.kotlin.extension.buildAndRegister

object WarpCommand : VarpCommand {

    override fun register(manager: CommandManager<out VarpCommandSourceStack>, serverProvider: () -> VarpServerImpl) {
        manager.buildAndRegister("warp") {
            permission("varp.self")

            required("warp", warpParser { serverProvider().tree })

            handler { context ->
                val warp: Warp = context["warp"]
                val player = context.sender().playerOrThrow()

                // Teleport the player to the warp.
                player.teleportToWarp(warp)
            }
        }
    }
}
