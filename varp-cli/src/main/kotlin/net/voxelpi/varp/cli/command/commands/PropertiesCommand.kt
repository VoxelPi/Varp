package net.voxelpi.varp.cli.command.commands

import kotlinx.coroutines.runBlocking
import net.voxelpi.event.annotation.Subscribe
import net.voxelpi.varp.cli.command.CommandsRegistrationEvent
import net.voxelpi.varp.extras.cloud.parser.tree.nodeParser
import net.voxelpi.varp.tree.Node
import net.voxelpi.varp.tree.NodeParent
import net.voxelpi.varp.tree.Warp
import org.incendo.cloud.description.Description
import org.incendo.cloud.kotlin.extension.buildAndRegister
import org.incendo.cloud.kotlin.extension.suggestionProvider
import org.incendo.cloud.parser.standard.StringParser.greedyStringParser
import org.incendo.cloud.parser.standard.StringParser.quotedStringParser
import org.incendo.cloud.suggestion.SuggestionProvider

object PropertiesCommand {

    @Subscribe
    fun handle(event: CommandsRegistrationEvent) {
        val cli = event.cli
        val commandManager = event.commandManager

        commandManager.buildAndRegister("properties", Description.description("Lists the properties of a node"), arrayOf("prop")) {
            required("node", nodeParser { cli.tree })

            handler { context ->
                val node: Node = context["node"]

                val propertyList = node.properties.map { "\"${it.key}\": \"${it.value}\"" }.joinToString(", ")
                context.sender().sendMessage("The node ${node.path} has the following ${node.properties.size} properties: $propertyList")
            }

            registerCopy("list") {}
        }

        commandManager.buildAndRegister("properties", Description.description("Checks if a node has a given property set"), arrayOf("prop")) {
            required("node", nodeParser { cli.tree })
            literal("has")
            required("key", quotedStringParser()) {
                suggestionProvider = SuggestionProvider.blockingStrings { context, input ->
                    context.get<Node>("node").properties.keys.map {
                        if (it.contains(' ')) "\"$it\"" else it
                    }
                }
            }

            handler { context ->
                val node: Node = context["node"]
                val key: String = context["key"]

                if (key in node.properties) {
                    context.sender().sendMessage("The node ${node.path} has the property \"${key}\"")
                } else {
                    context.sender().sendMessage("The node ${node.path} does not have a property \"${key}\"")
                }
            }
        }

        commandManager.buildAndRegister("properties", Description.description("Gets the value of a given property of a node"), arrayOf("prop")) {
            required("node", nodeParser { cli.tree })
            literal("get")
            required("key", quotedStringParser()) {
                suggestionProvider = SuggestionProvider.blockingStrings { context, input ->
                    context.get<Node>("node").properties.keys.map {
                        if (it.contains(' ')) "\"$it\"" else it
                    }
                }
            }

            handler { context ->
                val node: Node = context["node"]
                val key: String = context["key"]

                // Check if the node has the given property.
                if (key !in node.properties) {
                    context.sender().sendMessage("The node ${node.path} does not have a property \"${key}\"")
                    return@handler
                }

                context.sender().sendMessage("The node ${node.path} has the property \"${key}\" set to \"${node.properties[key]}\"")
            }
        }

        commandManager.buildAndRegister("properties", Description.description("Sets the given property of a node to the given value"), arrayOf("prop")) {
            required("node", nodeParser { cli.tree })
            literal("set")
            required("key", quotedStringParser())
            required("value", greedyStringParser())

            handler { context ->
                val node: Node = context["node"]
                val key: String = context["key"]
                val value: String = context["value"]

                runBlocking {
                    when (node) {
                        is Warp -> node.modify {
                            properties[key] = value
                        }
                        is NodeParent -> node.modify {
                            properties[key] = value
                        }
                    }
                }
                context.sender().sendMessage("Added the property \"$key\": \"$value\" to the node ${node.path}")
            }
        }

        commandManager.buildAndRegister("properties", Description.description("Removes the given property from a node"), arrayOf("prop")) {
            required("node", nodeParser { cli.tree })
            literal("remove")
            required("key", quotedStringParser()) {
                suggestionProvider = SuggestionProvider.blockingStrings { context, input ->
                    context.get<Node>("node").properties.keys.map {
                        if (it.contains(' ')) "\"$it\"" else it
                    }
                }
            }

            handler { context ->
                val node: Node = context["node"]
                val key: String = context["key"]

                // Check if the node has the given property.
                if (key !in node.properties) {
                    context.sender().sendMessage("The node ${node.path} does not have a property \"${key}\"")
                    return@handler
                }

                runBlocking {
                    when (node) {
                        is Warp -> {
                            node.modify {
                                removeProperty(key)
                            }
                        }
                        is NodeParent -> {
                            node.modify {
                                removeProperty(key)
                            }
                        }
                    }
                }
                context.sender().sendMessage("Removed the property \"$key\" from the node ${node.path}")
            }
        }
    }
}
