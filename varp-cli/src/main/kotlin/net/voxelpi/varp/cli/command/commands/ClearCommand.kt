package net.voxelpi.varp.cli.command.commands

import net.voxelpi.event.annotation.Subscribe
import net.voxelpi.varp.cli.command.CommandsRegistrationEvent
import org.incendo.cloud.description.Description
import org.incendo.cloud.kotlin.extension.buildAndRegister

object ClearCommand {

    @Subscribe
    fun handle(event: CommandsRegistrationEvent) {
        val cli = event.cli
        val commandManager = event.commandManager

        commandManager.buildAndRegister("clear", Description.description("Clears the console")) {
            handler { _ ->
                cli.console.clear()
                cli.console.printHeader()
            }
        }
    }
}
