package net.voxelpi.varp.cli.command.commands

import kotlinx.coroutines.runBlocking
import net.voxelpi.event.annotation.Subscribe
import net.voxelpi.varp.cli.command.CommandsRegistrationEvent
import net.voxelpi.varp.extras.cloud.parser.tree.nodeParser
import net.voxelpi.varp.warp.Node
import net.voxelpi.varp.warp.NodeParent
import net.voxelpi.varp.warp.Warp
import org.incendo.cloud.description.Description
import org.incendo.cloud.kotlin.extension.buildAndRegister
import org.incendo.cloud.kotlin.extension.suggestionProvider
import org.incendo.cloud.parser.standard.StringParser.quotedStringParser
import org.incendo.cloud.suggestion.SuggestionProvider

object TagsCommand {

    @Subscribe
    fun handle(event: CommandsRegistrationEvent) {
        val cli = event.cli
        val commandManager = event.commandManager

        commandManager.buildAndRegister("tags", Description.description("Lists the tags of a node"), arrayOf("tag")) {
            required("node", nodeParser { cli.tree })

            handler { context ->
                val node: Node = context["node"]

                val tagList = node.tags.joinToString(", ") { "\"$it\"" }
                context.sender().sendMessage("The node ${node.path} has the following ${node.tags.size} tags: $tagList")
            }

            registerCopy("list") {}
        }

        commandManager.buildAndRegister("tags", Description.description("Checks for a tag on a node"), arrayOf("tag")) {
            required("node", nodeParser { cli.tree })
            literal("has")
            required("tag", quotedStringParser()) {
                suggestionProvider = SuggestionProvider.blockingStrings { context, input ->
                    context.get<Node>("node").tags.map {
                        if (it.contains(' ')) "\"$it\"" else it
                    }
                }
            }

            handler { context ->
                val node: Node = context["node"]
                val tag: String = context["tag"]

                if (tag in node.tags) {
                    context.sender().sendMessage("The node ${node.path} has the tag \"${tag}\"")
                } else {
                    context.sender().sendMessage("The node ${node.path} does not have a tag \"${tag}\"")
                }
            }
        }

        commandManager.buildAndRegister("tags", Description.description("Adds a tag to a node"), arrayOf("tag")) {
            required("node", nodeParser { cli.tree })
            literal("add")
            required("tag", quotedStringParser())

            handler { context ->
                val node: Node = context["node"]
                val tag: String = context["tag"]

                runBlocking {
                    when (node) {
                        is Warp -> node.modify {
                            tags += tag
                        }
                        is NodeParent -> node.modify {
                            tags += tag
                        }
                    }
                }
                context.sender().sendMessage("Added the tag \"$tag\" to the node ${node.path}")
            }
        }

        commandManager.buildAndRegister("tags", Description.description("Removes a tag from a node"), arrayOf("tag")) {
            required("node", nodeParser { cli.tree })
            literal("remove")
            required("tag", quotedStringParser()) {
                suggestionProvider = SuggestionProvider.blockingStrings { context, input ->
                    context.get<Node>("node").tags.map {
                        if (it.contains(' ')) "\"$it\"" else it
                    }
                }
            }

            handler { context ->
                val node: Node = context["node"]
                val tag: String = context["tag"]

                // Check if the node has the given tag.
                if (tag !in node.tags) {
                    context.sender().sendMessage("The node ${node.path} does not have a tag \"${tag}\"")
                    return@handler
                }

                runBlocking {
                    when (node) {
                        is Warp -> node.modify {
                            tags -= tag
                        }
                        is NodeParent -> node.modify {
                            tags -= tag
                        }
                    }
                }
                context.sender().sendMessage("Remove the tag \"$tag\" to the node ${node.path}")
            }
        }
    }
}
