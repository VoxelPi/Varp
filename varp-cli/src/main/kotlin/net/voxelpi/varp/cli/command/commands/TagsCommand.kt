package net.voxelpi.varp.cli.command.commands

import kotlinx.coroutines.runBlocking
import net.voxelpi.event.annotation.Subscribe
import net.voxelpi.varp.cli.command.CommandsRegistrationEvent
import net.voxelpi.varp.cli.command.parser.tree.nodeChildParser
import net.voxelpi.varp.warp.Folder
import net.voxelpi.varp.warp.NodeChild
import net.voxelpi.varp.warp.Warp
import org.incendo.cloud.description.Description
import org.incendo.cloud.kotlin.extension.buildAndRegister
import org.incendo.cloud.parser.standard.StringParser.quotedStringParser

object TagsCommand {

    @Subscribe
    fun handle(event: CommandsRegistrationEvent) {
        val cli = event.cli
        val commandManager = event.commandManager

        commandManager.buildAndRegister("tag", Description.description("Tags a node")) {
            required("node", nodeChildParser { cli.tree })
            required("tag", quotedStringParser())

            handler { context ->
                val node: NodeChild = context["node"]
                val tag: String = context["tag"]

                runBlocking {
                    when (node) {
                        is Folder -> node.modify {
                            tags += tag
                        }
                        is Warp -> node.modify {
                            tags += tag
                        }
                    }
                }
            }
        }

        commandManager.buildAndRegister("tags", Description.description("Tags a node")) {
            required("node", nodeChildParser { cli.tree })

            handler { context ->
                val node: NodeChild = context["node"]

                context.sender().sendMessage("The node \"${node.path}\" has the following ${node.state.tags.size} tags: ${node.state.tags.map { "\"$it\"" }.joinToString(", ")}")
            }
        }
    }
}
