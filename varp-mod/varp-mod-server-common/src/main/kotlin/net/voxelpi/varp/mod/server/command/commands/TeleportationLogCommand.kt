package net.voxelpi.varp.mod.server.command.commands

import net.voxelpi.varp.mod.server.VarpServerImpl
import net.voxelpi.varp.mod.server.command.VarpCommand
import net.voxelpi.varp.mod.server.command.VarpCommandSourceStack
import org.incendo.cloud.CommandManager
import org.incendo.cloud.kotlin.extension.buildAndRegister
import org.incendo.cloud.parser.standard.IntegerParser.integerParser

object TeleportationLogCommand : VarpCommand {

    override fun register(manager: CommandManager<out VarpCommandSourceStack>, serverProvider: () -> VarpServerImpl) {
        manager.buildAndRegister("back", aliases = arrayOf("tpu", "tpundo")) {
            optional("steps", integerParser(1))

            handler { context ->
                val server = serverProvider()
                val player = context.sender().playerOrThrow()

                val steps: Int = context.getOrDefault("steps", 1)
                val actualSteps = player.undoTeleportationLogEntries(steps)
                if (actualSteps != steps) {
                    server.messages.sendErrorTeleportationLogNoPreviousEntry(player)
                    if (actualSteps <= 0) {
                        return@handler
                    }
                }
                // TODO: Play sound
                server.messages.sendTeleportationLogSendToPreviousEntry(player, actualSteps)
            }
        }

        manager.buildAndRegister("next", aliases = arrayOf("tpr", "tpredo")) {
            optional("steps", integerParser(1))

            handler { context ->
                val server = serverProvider()
                val player = context.sender().playerOrThrow()

                val steps: Int = context.getOrDefault("steps", 1)
                val actualSteps = player.redoTeleportationLogEntries(steps)
                if (actualSteps != steps) {
                    server.messages.sendErrorTeleportationLogNoNextEntry(player)
                    if (actualSteps <= 0) {
                        return@handler
                    }
                }
                // TODO: Play sound
                server.messages.sendTeleportationLogSendToNextEntry(player, actualSteps)
            }
        }
    }
}
