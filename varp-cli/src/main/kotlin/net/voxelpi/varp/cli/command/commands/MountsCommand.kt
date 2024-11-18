package net.voxelpi.varp.cli.command.commands

import net.voxelpi.event.annotation.Subscribe
import net.voxelpi.varp.cli.command.CommandsRegistrationEvent
import org.incendo.cloud.description.Description
import org.incendo.cloud.kotlin.extension.buildAndRegister

object MountsCommand {

    @Subscribe
    fun handle(event: CommandsRegistrationEvent) {
        val cli = event.cli
        val commandManager = event.commandManager

        commandManager.buildAndRegister("mounts", Description.description("Lists all loaded mounts")) {
            handler { context ->
                context.sender().sendMessage("The following mounts are loaded: ${cli.loader.compositor.mounts().joinToString(", ") { "\"${it.repository.id}\" at \"${it.path}\"" }}")
            }
        }
    }
}
