package net.voxelpi.varp.cli.command

import net.voxelpi.varp.cli.VarpCLI

@JvmRecord
data class CommandsRegistrationEvent(
    val cli: VarpCLI,
    val commandManager: VarpCLICommandManager,
)
