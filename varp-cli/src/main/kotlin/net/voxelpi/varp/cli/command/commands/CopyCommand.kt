package net.voxelpi.varp.cli.command.commands

import kotlinx.coroutines.runBlocking
import net.voxelpi.event.annotation.Subscribe
import net.voxelpi.varp.cli.command.CommandsRegistrationEvent
import net.voxelpi.varp.extras.cloud.parser.path.nodeParentPathParser
import net.voxelpi.varp.extras.cloud.parser.tree.nodeChildParser
import net.voxelpi.varp.tree.NodeChild
import net.voxelpi.varp.tree.path.NodeParentPath
import org.incendo.cloud.description.Description
import org.incendo.cloud.kotlin.extension.buildAndRegister
import org.incendo.cloud.parser.standard.StringParser.stringParser
import kotlin.jvm.optionals.getOrNull

object CopyCommand {

    @Subscribe
    fun handle(event: CommandsRegistrationEvent) {
        val cli = event.cli
        val commandManager = event.commandManager

        commandManager.buildAndRegister("copy", Description.description("Copy tree elements"), arrayOf("cp")) {
            required("node", nodeChildParser { cli.tree })
            required("destination", nodeParentPathParser { cli.tree })
            optional("id", stringParser())

            handler { context ->
                val node: NodeChild = context["node"]
                val destination: NodeParentPath = context["destination"]
                val id = context.optional<String>("id").getOrNull()

                runBlocking {
                    node.copy(destination, id).getOrThrow()
                }
            }
        }
    }
}
