package net.voxelpi.varp.mod.server.command.commands

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.kyori.adventure.text.minimessage.MiniMessage.miniMessage
import net.voxelpi.varp.extras.cloud.VarpCommandArguments
import net.voxelpi.varp.extras.cloud.parser.path.nodeParentPathParser
import net.voxelpi.varp.extras.cloud.parser.repositoryParser
import net.voxelpi.varp.mod.server.command.VarpCommand
import net.voxelpi.varp.mod.server.command.VarpCommandSourceStack
import net.voxelpi.varp.mod.server.command.VarpModCommandArguments
import net.voxelpi.varp.repository.Repository
import net.voxelpi.varp.tree.path.NodeParentPath
import net.voxelpi.varp.tree.path.RootPath
import org.incendo.cloud.CommandManager
import org.incendo.cloud.description.Description
import org.incendo.cloud.kotlin.extension.argumentDescription
import org.incendo.cloud.kotlin.extension.buildAndRegister
import org.incendo.cloud.parser.standard.StringParser.quotedStringParser
import org.incendo.cloud.parser.standard.StringParser.stringParser
import kotlin.jvm.optionals.getOrNull

object RepositoryCommand : VarpCommand {

    override fun register(manager: CommandManager<out VarpCommandSourceStack>) {
        manager.buildAndRegister("varp", aliases = arrayOf("warpmanager", "wm")) {
            literal("repository", Description.empty(), "repos")

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

        manager.buildAndRegister("varp", aliases = arrayOf("warpmanager", "wm")) {
            literal("repository", Description.empty(), "repos")
            literal("mount")
            required("repository", repositoryParser())
            required("repository_path", nodeParentPathParser { context -> context.get<Repository>("repository").tree })
            literal("at")
            required("mount_location", nodeParentPathParser())
            required("mount_id", stringParser())

            flag("name", arrayOf("n"), argumentDescription("The name of the mount"), quotedStringParser())

            handler { context ->
                val server = context[VarpModCommandArguments.SERVER]
                val environment = context[VarpCommandArguments.ENVIRONMENT]
                val coroutineScope = context[VarpModCommandArguments.COROUTINE_SCOPE]
                val messages = context[VarpModCommandArguments.MESSAGE_SERVICE]

                val repository: Repository = context["repository"]
                val repositoryPath: NodeParentPath = context.getOrDefault("repository_path", RootPath)
                val mountLocation: NodeParentPath = context["mount_location"]
                val mountId: String = context["mount_id"]
                val mountPath = mountLocation.folder(mountId)

                val name = context.flags().getValue<String>("name").getOrNull()?.let(miniMessage()::deserialize)

                coroutineScope.launch(Dispatchers.IO) {
                    environment.compositor.modifyMounts {
                        register(mountPath, repository, repositoryPath) {
                            this.name = name
                        }
                    }.onFailure {
                        messages.sendErrorGeneric(context.sender())
                        return@launch
                    }
                    server.saveVarpEnvironment().onFailure {
                        messages.sendErrorGeneric(context.sender())
                        server.logger.warn("Failed to save varp environment", it)
                        return@launch
                    }
                    messages.sendRepositoryMounted(context.sender(), mountPath, repository, repositoryPath)
                }
            }
        }
    }
}
