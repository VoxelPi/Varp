package net.voxelpi.varp.mod.server.command.commands

import net.voxelpi.varp.mod.server.VarpServerImpl
import net.voxelpi.varp.mod.server.command.VarpCommand
import net.voxelpi.varp.mod.server.command.VarpCommandSourceStack
import org.incendo.cloud.CommandManager
import org.incendo.cloud.kotlin.extension.buildAndRegister

object ClientCommand : VarpCommand {

    override fun register(manager: CommandManager<out VarpCommandSourceStack>, serverProvider: () -> VarpServerImpl) {
        manager.buildAndRegister("varp", aliases = arrayOf("warpmanager", "wm")) {
            literal("client")

            handler { context ->
                val server = serverProvider()
                val player = context.sender().playerOrThrow()

                val clientInformation = player.clientInformation
                if (clientInformation == null) {
                    server.messages.sendClientInfoBridgeDisabled(context.sender().sender)
                    return@handler
                }

                server.messages.sendClientInfoBridgeEnabled(context.sender().sender, clientInformation)
            }

            registerCopy("info") {}
        }
    }
}
