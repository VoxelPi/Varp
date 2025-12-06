package net.voxelpi.varp.mod.server.command.commands

import net.voxelpi.varp.mod.server.VarpServerImpl
import net.voxelpi.varp.mod.server.command.VarpCommand
import net.voxelpi.varp.mod.server.command.VarpCommandSourceStack
import org.incendo.cloud.CommandManager
import org.incendo.cloud.description.Description
import org.incendo.cloud.kotlin.extension.buildAndRegister

object RepositoriesCommand : VarpCommand {

    override fun register(manager: CommandManager<out VarpCommandSourceStack>, serverProvider: () -> VarpServerImpl) {
        manager.buildAndRegister("varp", aliases = arrayOf("warpmanager", "wm")) {
            literal("repositories", Description.empty(), "repos")

            handler { context ->
                val server = serverProvider()
                val sender = context.sender()

                server.messages.sendRepositoryListHeader(sender, server.loader.repositories().size)
                for (repository in server.loader.repositories()) {
                    val mounts = server.compositor.mounts().filter { it.repository.id == repository.id }
                    if (mounts.isEmpty()) {
                        server.messages.sendRepositoryListEntryWithoutMounts(sender, repository.id, repository.type.id)
                    } else {
                        val mountsText = mounts.joinToString(",") { it.path.toString() }
                        server.messages.sendRepositoryListEntryWithMounts(sender, repository.id, repository.type.id, mountsText)
                    }
                }
            }

            registerCopy("list") {}
        }
    }
}
