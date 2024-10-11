package net.voxelpi.varp.cli.command

import net.voxelpi.event.post
import net.voxelpi.varp.cli.VarpCLI
import net.voxelpi.varp.cli.command.commands.ClearCommand
import net.voxelpi.varp.cli.command.commands.StopCommand
import org.incendo.cloud.CommandManager
import org.incendo.cloud.execution.ExecutionCoordinator
import org.incendo.cloud.internal.CommandRegistrationHandler
import kotlin.reflect.full.createInstance

class VarpCLICommandManager(
    private val cli: VarpCLI,
) : CommandManager<VarpCLICommandSender>(ExecutionCoordinator.simpleCoordinator(), CommandRegistrationHandler.nullCommandRegistrationHandler()) {

    private val eventScope = cli.eventScope.createSubScope()

    init {
        // Register all internal commands.
        registerCommands()

        // Post registration event.
        eventScope.post(CommandsRegistrationEvent(cli, this))
    }

    override fun hasPermission(sender: VarpCLICommandSender, permission: String): Boolean {
        return true
    }

    private fun registerCommands() {
        registerCommand(ClearCommand)
        registerCommand(StopCommand)
    }

    private fun registerCommand(instance: Any) {
        eventScope.registerAnnotated(instance)
    }

    private inline fun <reified C : Any> registerCommand() {
        val instance = C::class.createInstance()
        eventScope.registerAnnotated(instance)
    }
}
