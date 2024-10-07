package net.voxelpi.varp.mod.paper.command

import io.papermc.paper.command.brigadier.CommandSourceStack
import net.voxelpi.varp.mod.server.command.VarpCommandSourceStack

@Suppress("UnstableApiUsage")
class PaperVarpCommandSourceStack(
    val sourceStack: CommandSourceStack,
) : VarpCommandSourceStack
