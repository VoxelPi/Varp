package net.voxelpi.varp.mod.server.command.commands

import net.voxelpi.varp.extras.cloud.VarpCommandArguments
import net.voxelpi.varp.mod.server.command.VarpCommand
import net.voxelpi.varp.mod.server.command.VarpCommandSourceStack
import net.voxelpi.varp.mod.server.command.VarpModCommandArguments
import org.incendo.cloud.CommandManager
import org.incendo.cloud.kotlin.extension.buildAndRegister

object MountCommand : VarpCommand {

    override fun register(manager: CommandManager<out VarpCommandSourceStack>) {
        manager.buildAndRegister("varp", aliases = arrayOf("warpmanager", "wm")) {
            literal("mount")

            handler { context ->
                val messages = context[VarpModCommandArguments.MESSAGE_SERVICE]
                val compositor = context[VarpCommandArguments.COMPOSITOR]
                val mounts = compositor.mounts()

                messages.sendMountListHeader(context.sender(), mounts.size)
                for (mount in mounts) {
                    messages.sendMountListEntry(context.sender(), mount.path.toString(), mount.repository.id, mount.sourcePath.toString())
                }
            }

            registerCopy("list") {}
        }
    }
}
