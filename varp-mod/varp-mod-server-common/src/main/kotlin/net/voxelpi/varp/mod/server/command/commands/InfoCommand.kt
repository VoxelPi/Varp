package net.voxelpi.varp.mod.server.command.commands

import net.voxelpi.varp.mod.server.VarpServerImpl
import net.voxelpi.varp.mod.server.command.VarpCommand
import net.voxelpi.varp.mod.server.command.VarpCommandSourceStack
import org.incendo.cloud.CommandManager
import org.incendo.cloud.kotlin.extension.buildAndRegister

object InfoCommand : VarpCommand {

    override fun register(manager: CommandManager<out VarpCommandSourceStack>, serverProvider: () -> VarpServerImpl) {
        manager.buildAndRegister("varp", aliases = arrayOf("warpmanager", "wm")) {
            handler { context ->
                val server = serverProvider()

                server.messages.sendVarpInfo(
                    context.sender().sender,
                    server.version,
                    server.platform.name,
                    server.platform.brand,
                    server.platform.version,
                )
            }
        }

        manager.buildAndRegister("varp", aliases = arrayOf("warpmanager", "wm")) {
            literal("info")

            handler { context ->
                val server = serverProvider()

                server.messages.sendVarpInfo(
                    context.sender().sender,
                    server.version,
                    server.platform.name,
                    server.platform.brand,
                    server.platform.version,
                )
            }
        }

        manager.buildAndRegister("varp", aliases = arrayOf("warpmanager", "wm")) {
            literal("version")

            handler { context ->
                val server = serverProvider()

                server.messages.sendVarpInfo(
                    context.sender().sender,
                    server.version,
                    server.platform.name,
                    server.platform.brand,
                    server.platform.version,
                )
            }
        }
    }
}
