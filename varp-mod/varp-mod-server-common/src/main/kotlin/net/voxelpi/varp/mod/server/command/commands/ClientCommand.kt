package net.voxelpi.varp.mod.server.command.commands

import net.voxelpi.varp.mod.server.command.VarpCommand
import net.voxelpi.varp.mod.server.command.VarpCommandSourceStack
import net.voxelpi.varp.mod.server.command.VarpModCommandArguments
import org.incendo.cloud.CommandManager
import org.incendo.cloud.kotlin.extension.buildAndRegister

object ClientCommand : VarpCommand {

    override fun register(manager: CommandManager<out VarpCommandSourceStack>) {
        manager.buildAndRegister("varp", aliases = arrayOf("warpmanager", "wm")) {
            literal("client")

            handler { context ->
                val messages = context[VarpModCommandArguments.MESSAGE_SERVICE]
                val player = context.sender().playerOrThrow()

                val clientInformation = player.clientInformation
                if (clientInformation == null) {
                    messages.sendClientInfoBridgeDisabled(context.sender().sender)
                    return@handler
                }

                messages.sendClientInfoBridgeEnabled(context.sender().sender, clientInformation)
            }

            registerCopy("info") {}
        }
    }
}
