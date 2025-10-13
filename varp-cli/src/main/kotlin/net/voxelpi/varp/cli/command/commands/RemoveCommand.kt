package net.voxelpi.varp.cli.command.commands

import kotlinx.coroutines.runBlocking
import net.voxelpi.event.annotation.Subscribe
import net.voxelpi.varp.cli.command.CommandsRegistrationEvent
import net.voxelpi.varp.extras.cloud.parser.tree.nodeChildParser
import net.voxelpi.varp.warp.NodeChild
import org.incendo.cloud.description.Description
import org.incendo.cloud.kotlin.extension.buildAndRegister

object RemoveCommand {

    @Subscribe
    fun handle(event: CommandsRegistrationEvent) {
        val cli = event.cli
        val commandManager = event.commandManager

        commandManager.buildAndRegister("remove", Description.description("Remvoes tree elements"), arrayOf("rm")) {
            required("node", nodeChildParser { cli.tree })

            handler { context ->
                val node: NodeChild = context["node"]

                runBlocking {
                    node.delete().getOrThrow()
                }
            }
        }
    }
}
