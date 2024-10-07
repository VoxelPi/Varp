package net.voxelpi.varp.mod.server.command

import org.incendo.cloud.CommandManager

interface VarpCommandService {

    val commandManager: CommandManager<out VarpCommandSourceStack>
}
