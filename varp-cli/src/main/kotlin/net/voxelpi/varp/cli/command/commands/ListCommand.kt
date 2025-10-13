package net.voxelpi.varp.cli.command.commands

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.format.NamedTextColor
import net.voxelpi.event.annotation.Subscribe
import net.voxelpi.varp.cli.command.CommandsRegistrationEvent
import net.voxelpi.varp.extras.cloud.parser.tree.nodeParentParser
import net.voxelpi.varp.warp.Node
import net.voxelpi.varp.warp.NodeParent
import org.incendo.cloud.kotlin.extension.argumentDescription
import org.incendo.cloud.kotlin.extension.buildAndRegister

object ListCommand {

    @Subscribe
    fun handle(event: CommandsRegistrationEvent) {
        val cli = event.cli
        val commandManager = event.commandManager

        commandManager.buildAndRegister("list", argumentDescription("Lists the tree"), arrayOf("ls")) {
            optional("path", nodeParentParser { cli.tree })

            handler { context ->
                val tree = cli.tree
                val parent = context.getOrDefault<NodeParent>("path", tree.root)

                val lines = mutableListOf<Pair<String, Node>>()
                fillListLinesForContainer(lines, parent, "  ")

                val maxWidth = lines.maxOf { it.first.length + it.second.path.key.length } + 4
                val output = lines.map { (prefix, node) ->
                    Component.textOfChildren(
                        Component.text(prefix).color(NamedTextColor.GRAY),
                        Component.text(node.path.key + " "),
                        Component.text(".".repeat(maxWidth - prefix.length - node.path.key.length)).color(NamedTextColor.GRAY),
                        Component.text(" "),
                        node.state.name,
                    )
                }

                context.sender().sendMessage(
                    Component.textOfChildren(
                        Component.text("List of all nodes:\n"),
                        Component.join(
                            JoinConfiguration.newlines(),
                            output,
                        )
                    )
                )
            }
        }
    }

    private fun fillListLinesForContainer(lines: MutableList<Pair<String, Node>>, container: NodeParent, indention: String) {
        lines.add(indention to container)

        var childIndention = indention
        if (indention.isNotEmpty()) {
            childIndention = indention.substring(0, indention.length - 2) + if (indention[indention.length - 2] == '├') "│ " else "  "
        }

        val childFolders = container.childFolders().sortedBy { it.id }
        val childWarps = container.childWarps().sortedBy { it.id }
        val n = childFolders.size + childWarps.size
        val nWarps = childWarps.size

        for ((i, folder) in childFolders.withIndex()) {
            val prefix = if (i < n - 1) "├" else "└"
            fillListLinesForContainer(lines, folder, "$childIndention$prefix ")
        }

        for ((i, warp) in childWarps.withIndex()) {
            val prefix = if (i < nWarps - 1) "├" else "└"
            lines.add("$childIndention$prefix " to warp)
        }
    }
}
