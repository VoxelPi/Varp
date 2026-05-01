package net.voxelpi.varp.mod.server.command

import org.incendo.cloud.CommandManager

interface VarpCommand {

    fun register(manager: CommandManager<out VarpCommandSourceStack>)
}
