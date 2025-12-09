package net.voxelpi.varp.mod.server.command.commands

import net.voxelpi.varp.mod.server.VarpServerImpl
import net.voxelpi.varp.mod.server.command.VarpCommand
import net.voxelpi.varp.mod.server.command.VarpCommandSourceStack
import org.incendo.cloud.CommandManager
import org.incendo.cloud.kotlin.extension.buildAndRegister

object MountsCommand : VarpCommand {

    override fun register(manager: CommandManager<out VarpCommandSourceStack>, serverProvider: () -> VarpServerImpl) {
        manager.buildAndRegister("varp", aliases = arrayOf("warpmanager", "wm")) {
            literal("mounts")

            handler { context ->
                val server = serverProvider()
                val mounts = server.compositor.mounts()

                server.messages.sendMountListHeader(context.sender(), mounts.size)
                for (mount in mounts) {
                    server.messages.sendMountListEntry(context.sender(), mount.path.toString(), mount.repository.id, mount.sourcePath.toString())
                }
            }

            registerCopy("list") {}
        }
    }
}
