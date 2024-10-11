package net.voxelpi.varp.cli.command.commands

import net.voxelpi.event.annotation.Subscribe
import net.voxelpi.varp.cli.command.CommandsRegistrationEvent
import org.incendo.cloud.description.Description
import org.incendo.cloud.kotlin.extension.buildAndRegister

object StopCommand {

    @Subscribe
    fun handle(event: CommandsRegistrationEvent) {
        val cli = event.cli
        val commandManager = event.commandManager

        commandManager.buildAndRegister("stop", Description.description("Stops the cloud"), arrayOf("exit")) {
            handler { context ->
                cli.stop()
            }
        }
    }
}
