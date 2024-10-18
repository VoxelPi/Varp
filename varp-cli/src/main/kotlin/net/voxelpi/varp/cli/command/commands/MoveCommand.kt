package net.voxelpi.varp.cli.command.commands

import net.voxelpi.event.annotation.Subscribe
import net.voxelpi.varp.DuplicatesStrategy
import net.voxelpi.varp.cli.command.CommandsRegistrationEvent
import net.voxelpi.varp.cli.command.parser.path.nodeParentPathParser
import net.voxelpi.varp.cli.command.parser.tree.nodeChildParser
import net.voxelpi.varp.warp.NodeChild
import net.voxelpi.varp.warp.path.NodeParentPath
import org.incendo.cloud.description.Description
import org.incendo.cloud.kotlin.extension.buildAndRegister

object MoveCommand {

    @Subscribe
    fun handle(event: CommandsRegistrationEvent) {
        val cli = event.cli
        val commandManager = event.commandManager

        commandManager.buildAndRegister("move", Description.description("Moves tree elements"), arrayOf("mv")) {
            required("node", nodeChildParser { cli.tree })
            required("destination", nodeParentPathParser { cli.tree })

            handler { context ->
                val node: NodeChild = context["node"]
                val destination: NodeParentPath = context["destination"]

                node.move(destination, DuplicatesStrategy.FAIL).getOrThrow()
            }
        }
    }
}
