package net.voxelpi.varp.mod.server.command.commands

import net.voxelpi.varp.mod.server.command.VarpCommand
import net.voxelpi.varp.mod.server.command.VarpCommandSourceStack
import net.voxelpi.varp.mod.server.command.VarpModCommandArguments
import org.incendo.cloud.CommandManager
import org.incendo.cloud.kotlin.extension.buildAndRegister

object InfoCommand : VarpCommand {

    override fun register(manager: CommandManager<out VarpCommandSourceStack>) {
        manager.buildAndRegister("varp", aliases = arrayOf("warpmanager", "wm")) {
            handler { context ->
                val messages = context[VarpModCommandArguments.MESSAGE_SERVICE]
                val server = context[VarpModCommandArguments.SERVER]

                messages.sendVarpInfo(
                    context.sender().sender,
                    server.version,
                    server.platform.name,
                    server.platform.brand,
                    server.platform.version,
                )
            }

            registerCopy("info") {}
            registerCopy("version") {}
        }
    }
}
