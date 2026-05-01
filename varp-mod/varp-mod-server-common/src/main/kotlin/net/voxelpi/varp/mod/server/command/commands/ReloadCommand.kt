package net.voxelpi.varp.mod.server.command.commands

import net.voxelpi.varp.mod.server.command.VarpCommand
import net.voxelpi.varp.mod.server.command.VarpCommandSourceStack
import net.voxelpi.varp.mod.server.command.VarpModCommandArguments
import org.incendo.cloud.CommandManager
import org.incendo.cloud.kotlin.extension.buildAndRegister

object ReloadCommand : VarpCommand {

    override fun register(manager: CommandManager<out VarpCommandSourceStack>) {
        manager.buildAndRegister("varp", aliases = arrayOf("warpmanager", "wm")) {
            permission("varp.reload.messages")

            literal("reload")
            literal("messages")

            handler { context ->
                val server = context[VarpModCommandArguments.SERVER]

                // Reload all messages.
                server.loadMessages()
                server.messages.sendReloadMessages(context.sender())
            }
        }
    }
}
