package net.voxelpi.varp.mod.server.command.commands

import net.voxelpi.varp.extras.cloud.VarpCommandArguments
import net.voxelpi.varp.mod.server.command.VarpCommand
import net.voxelpi.varp.mod.server.command.VarpCommandSourceStack
import net.voxelpi.varp.mod.server.command.VarpModCommandArguments
import org.incendo.cloud.CommandManager
import org.incendo.cloud.description.Description
import org.incendo.cloud.kotlin.extension.buildAndRegister

object RepositoriesCommand : VarpCommand {

    override fun register(manager: CommandManager<out VarpCommandSourceStack>) {
        manager.buildAndRegister("varp", aliases = arrayOf("warpmanager", "wm")) {
            literal("repositories", Description.empty(), "repos")

            handler { context ->
                val messages = context[VarpModCommandArguments.MESSAGE_SERVICE]
                val environment = context[VarpCommandArguments.ENVIRONMENT]
                val sender = context.sender()

                messages.sendRepositoryListHeader(sender, environment.repositories.size)
                for (repository in environment.repositories.values) {
                    val mounts = environment.compositor.mounts().filter { it.repository.id == repository.id }
                    if (mounts.isEmpty()) {
                        messages.sendRepositoryListEntryWithoutMounts(sender, repository.id, repository.type.id)
                    } else {
                        val mountsText = mounts.joinToString(",") { it.path.toString() }
                        messages.sendRepositoryListEntryWithMounts(sender, repository.id, repository.type.id, mountsText)
                    }
                }
            }

            registerCopy("list") {}
        }
    }
}
