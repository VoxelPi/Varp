package net.voxelpi.varp.cli.command.commands

import net.voxelpi.event.annotation.Subscribe
import net.voxelpi.varp.cli.command.CommandsRegistrationEvent
import org.incendo.cloud.description.Description
import org.incendo.cloud.kotlin.extension.buildAndRegister

object RepositoriesCommand {

    @Subscribe
    fun handle(event: CommandsRegistrationEvent) {
        val cli = event.cli
        val commandManager = event.commandManager

        commandManager.buildAndRegister("repositories", Description.description("Lists all loaded repositories"), arrayOf("repos")) {
            handler { context ->
                context.sender().sendMessage("The following repositories are loaded: ${cli.environment.repositories.keys.joinToString(", ")}")
            }
        }
    }
}
