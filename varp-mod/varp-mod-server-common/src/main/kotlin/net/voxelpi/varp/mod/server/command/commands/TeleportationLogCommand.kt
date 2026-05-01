package net.voxelpi.varp.mod.server.command.commands

import net.voxelpi.varp.mod.server.command.VarpCommand
import net.voxelpi.varp.mod.server.command.VarpCommandSourceStack
import net.voxelpi.varp.mod.server.command.VarpModCommandArguments
import org.incendo.cloud.CommandManager
import org.incendo.cloud.kotlin.extension.buildAndRegister
import org.incendo.cloud.parser.standard.IntegerParser.integerParser

object TeleportationLogCommand : VarpCommand {

    override fun register(manager: CommandManager<out VarpCommandSourceStack>) {
        manager.buildAndRegister("back", aliases = arrayOf("tpu", "tpundo")) {
            optional("steps", integerParser(1))

            handler { context ->
                val messages = context[VarpModCommandArguments.MESSAGE_SERVICE]
                val player = context.sender().playerOrThrow()

                val steps: Int = context.getOrDefault("steps", 1)
                val actualSteps = player.undoTeleportationLogEntries(steps)
                if (actualSteps != steps) {
                    messages.sendErrorTeleportationLogNoPreviousEntry(player)
                    if (actualSteps <= 0) {
                        return@handler
                    }
                }
                // TODO: Play sound
                messages.sendTeleportationLogSendToPreviousEntry(player, actualSteps)
            }
        }

        manager.buildAndRegister("next", aliases = arrayOf("tpr", "tpredo")) {
            optional("steps", integerParser(1))

            handler { context ->
                val messages = context[VarpModCommandArguments.MESSAGE_SERVICE]
                val player = context.sender().playerOrThrow()

                val steps: Int = context.getOrDefault("steps", 1)
                val actualSteps = player.redoTeleportationLogEntries(steps)
                if (actualSteps != steps) {
                    messages.sendErrorTeleportationLogNoNextEntry(player)
                    if (actualSteps <= 0) {
                        return@handler
                    }
                }
                // TODO: Play sound
                messages.sendTeleportationLogSendToNextEntry(player, actualSteps)
            }
        }
    }
}
