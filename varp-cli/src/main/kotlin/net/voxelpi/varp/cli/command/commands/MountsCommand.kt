package net.voxelpi.varp.cli.command.commands

import kotlinx.coroutines.runBlocking
import net.voxelpi.event.annotation.Subscribe
import net.voxelpi.varp.cli.command.CommandsRegistrationEvent
import net.voxelpi.varp.cli.command.parser.path.nodeParentPathParser
import net.voxelpi.varp.warp.path.NodeParentPath
import net.voxelpi.varp.warp.repository.Repository
import net.voxelpi.varp.warp.repository.compositor.CompositorMount
import org.incendo.cloud.description.Description
import org.incendo.cloud.kotlin.extension.buildAndRegister
import org.incendo.cloud.kotlin.extension.suggestionProvider
import org.incendo.cloud.parser.standard.StringParser.stringParser
import org.incendo.cloud.suggestion.SuggestionProvider

object MountsCommand {

    @Subscribe
    fun handle(event: CommandsRegistrationEvent) {
        val cli = event.cli
        val commandManager = event.commandManager

        commandManager.buildAndRegister("mounts", Description.description("Lists all loaded mounts")) {
            handler { context ->
                val mountsList = cli.loader.compositor.mounts().joinToString(", ") { "${it.sourcePath} of \"${it.repository.id}\" at ${it.path}" }
                context.sender().sendMessage("The following ${cli.loader.compositor.mounts().size} mounts are loaded: $mountsList")
            }

            registerCopy("list") {}
        }

        commandManager.buildAndRegister("mounts", Description.description("Adds a mount to the compositor")) {
            literal("add")

            required("repository", stringParser()) {
                suggestionProvider = SuggestionProvider.blockingStrings { context, input ->
                    cli.loader.repositories().map(Repository::id)
                }
            }
            required("source_path", nodeParentPathParser { context -> cli.loader.repository(context["repository"])?.tree })
            required("mount_parent_path", nodeParentPathParser { cli.tree })
            required("mount_id", stringParser()) {}

            handler { context ->
                val repositoryId: String = context["repository"]
                val repositoryPath: NodeParentPath = context["source_path"]
                val mountParentPath: NodeParentPath = context["mount_parent_path"]
                val mountId: String = context["mount_id"]

                val repository = cli.loader.repository(repositoryId)
                if (repository == null) {
                    context.sender().sendMessage("Unknown repository \"$repositoryId\"")
                    return@handler
                }

                val mountPath = mountParentPath.folder(mountId)

                runBlocking {
                    cli.loader.compositor.modifyMounts {
                        register(mountPath, repository, repositoryPath)
                    }.getOrThrow()
                }
                context.sender().sendMessage("Added mount to $repositoryPath of repository \"$repositoryId\" at $mountPath")
            }
        }

        commandManager.buildAndRegister("mounts", Description.description("Removes a mount from the compositor")) {
            literal("remove")
            required("mount_path", nodeParentPathParser { cli.tree }) {
                suggestionProvider = SuggestionProvider.blockingStrings { context, input ->
                    cli.loader.compositor.mounts().map(CompositorMount::path).map(NodeParentPath::toString)
                }
            }

            handler { context ->
                val mountPath: NodeParentPath = context["mount_path"]

                val mount = cli.loader.compositor.mount(mountPath)
                if (mount == null) {
                    context.sender().sendMessage("There is no mount at $mountPath")
                    return@handler
                }

                runBlocking {
                    cli.loader.compositor.modifyMounts {
                        unregister(mount.path)
                    }
                }
                context.sender().sendMessage("Removed mount to ${mount.sourcePath} of repository \"${mount.repository.id}\" at ${mount.path}")
            }
        }
    }
}
